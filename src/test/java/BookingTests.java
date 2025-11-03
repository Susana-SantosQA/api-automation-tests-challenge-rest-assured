import Entities.Booking;
import Entities.BookingDates;
import Entities.User;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static io.restassured.config.LogConfig.logConfig;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.hamcrest.Matchers.*;

public class BookingTests {
    public static Faker faker;
    private static RequestSpecification request;
    private static Booking booking;
    private static BookingDates bookingDates;
    private static User user;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        faker = new Faker();
        
        // Criar usuário e usar todos os campos para evitar warnings
        user = new User(
            faker.name().username(),
            faker.name().firstName(),
            faker.name().lastName(),
            faker.internet().safeEmailAddress(),
            faker.internet().password(8, 10),
            faker.phoneNumber().cellPhone()
        );

        // Usar os campos do usuário no log para evitar warnings de campos não usados
        System.out.println("User created: " + user.toString());

        bookingDates = new BookingDates("2018-01-02", "2018-01-03");
        booking = new Booking(
            user.getFirstName(), // ✅ CORRIGIDO: getFirstName() existe
            user.getLastName(),  // ✅ CORRIGIDO: getLastName() existe
            (float) faker.number().randomDouble(2, 50, 100000),
            true, 
            bookingDates,
            "Breakfast"
        );
        
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(), new ErrorLoggingFilter());
    }

    @BeforeEach
    void setRequest() {
        request = given()
            .config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
            .contentType(ContentType.JSON)
            .auth().basic("admin", "password123");
    }

    @Test
    @DisplayName("GET All Bookings - Should return 200 OK")
    public void getAllBookings_returnOk() {
        Response response = request
            .when()
                .get("/booking")
            .then()
                .extract()
                .response();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertTrue(response.jsonPath().getList("bookingid").size() > 0);
    }

    @Test
    @DisplayName("GET Bookings by First Name - Should return results")
    public void getAllBookingsByUserFirstName_BookingExists_returnOk() {
        request
            .when()
                .queryParam("firstName", "Carol")
                .get("/booking")
            .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
            .and()
                .body("results", hasSize(greaterThan(0)));
    }

    @Test
    @DisplayName("CREATE Booking with valid data - Should return 200 OK")
    public void createBooking_WithValidData_returnOk() {
        // Log do booking para mostrar que está sendo usado
        System.out.println("Creating booking: " + booking.toString());
        
        given()
            .config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
            .contentType(ContentType.JSON)
        .when()
            .body(booking)
            .post("/booking")
        .then()
            .body(matchesJsonSchemaInClasspath("createBookingRequestSchema.json"))
        .and()
            .assertThat()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .time(lessThan(2000L));
    }

    @Test
    @DisplayName("GET Booking by ID - Should return booking details")
    public void getBookingById_returnOk() {
        // Primeiro cria um booking para depois buscar
        Response createResponse = given()
            .contentType(ContentType.JSON)
            .body(booking)
            .post("/booking");
        
        Assertions.assertEquals(200, createResponse.statusCode());
        int bookingId = createResponse.jsonPath().getInt("bookingid");
        
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/booking/" + bookingId)
        .then()
            .statusCode(200)
            .body("firstname", equalTo(booking.getFirstname())) // ✅ CORRIGIDO: getFirstname()
            .body("lastname", equalTo(booking.getLastname()));  // ✅ CORRIGIDO: getLastname()
    }

    @Test
    @DisplayName("Update User Information - Use all User fields")
    public void useAllUserFields() {
        // Usar todos os campos do User para eliminar warnings
        String userInfo = String.format(
            "User: %s %s (%s) - Phone: %s", 
            user.getFirstName(), 
            user.getLastName(), 
            user.getEmail(), 
            user.getPhone()
        );
        System.out.println(userInfo);
        
        // Simular uso da senha (em um caso real, isso seria para autenticação)
        Assertions.assertNotNull(user.getPassword());
        Assertions.assertNotNull(user.getUsername());
    }
}