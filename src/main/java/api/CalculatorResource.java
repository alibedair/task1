package api;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ejbs.Calculation;

import java.util.List;

@Stateless
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CalculatorResource {

    @PersistenceContext
    private EntityManager entityManager;

    @POST
    @Path("/calc")
    public Response performCalculation(CalculationRequest request) {
        try {
            int number1 = Integer.parseInt(request.getNumber1());
            int number2 = Integer.parseInt(request.getNumber2());
            String operation = request.getOperation();

            int result = calculate(number1, number2, operation);

            // Save calculation to database
            Calculation calculation = new Calculation(number1, number2, operation);
            entityManager.persist(calculation);

            // Build response
            CalculationResponse response = new CalculationResponse(result);
            return Response.ok(response).build();
        } catch (ArithmeticException | IllegalArgumentException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/calculations")
    public Response getAllCalculations() {
        try {
            List<Calculation> calculations = entityManager.createQuery("SELECT c FROM Calculation c", Calculation.class)
                    .getResultList();
            return Response.ok(calculations).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private int calculate(int number1, int number2, String operation) {
        switch (operation) {
            case "+":
                return number1 + number2;
            case "-":
                return number1 - number2;
            case "*":
                return number1 * number2;
            case "/":
                if (number2 != 0) {
                    return number1 / number2;
                } else {
                    throw new ArithmeticException("Cannot divide by zero");
                }
            default:
                throw new IllegalArgumentException("Invalid operation: " + operation);
        }
    }

    public static class CalculationRequest {
        private String number1;
        private String number2;
        private String operation;

        // getters and setters
        public String getNumber1() {
            return number1;
        }

        public void setNumber1(String number1) {
            this.number1 = number1;
        }

        public String getNumber2() {
            return number2;
        }

        public void setNumber2(String number2) {
            this.number2 = number2;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }
    }

    public static class CalculationResponse {
        private int result;

        public CalculationResponse(int result) {
            this.result = result;
        }

        // getter for result
        public int getResult() {
            return result;
        }
    }
}
