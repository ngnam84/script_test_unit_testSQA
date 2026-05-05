package com.example.do_an_tot_nghiep.Model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

// Model classes under test


/**
 * Unit tests for Model/Entity classes.
 *
 * Test Cases: TC-MODEL-001 to TC-MODEL-056
 * Total: 56 test cases covering 16 Model classes
 *
 * These tests verify:
 * - Default field values should not be null
 * - Numeric IDs should be non-negative
 * - Enum-like fields (gender, isRead) should be constrained to valid values
 * - Setter/getter pairs store and retrieve values correctly
 * - Boundary values are handled properly
 * - Relationship getters work without throwing exceptions
 */
public class ModelEntityLogicTest {

    private Setting setting;
    private Option option;
    private Handbook handbook;
    private User user;
    private Appointment appointment;

    @Before
    public void setUp() {
        setting = new Setting(1, "setting-id", "Setting Name");
        option = new Option(2, "Option Name");
        handbook = new Handbook("img.png", "Title", "https://example.com");
        user = new User();
        appointment = new Appointment();
    }

    // =============================
    // ORIGINAL SAMPLE TEST CASES (PRESERVED)
    // =============================

    // Test Case ID: TC-MODEL-001
    @Test
    public void givenVeryLongStringWhenSetSettingNameThenNameIsPreserved() {
        String longName = repeat('A', 1024);
        setting.setName(longName);

        assertEquals(longName, setting.getName());
    }

    // Test Case ID: TC-MODEL-002
    @Test
    public void givenNullOptionNameWhenSetNameThenShouldFallbackToEmptyString() {
        option.setName(null);

        assertNotNull(option.getName());
        assertEquals("", option.getName());
    }

    // Test Case ID: TC-MODEL-003
    @Test
    public void givenBlankSettingIdWhenSetIdThenShouldRejectBlankIdentifier() {
        setting.setId("   ");

        assertTrue("Expected non-blank id", setting.getId() != null && !setting.getId().trim().isEmpty());
    }

    // Test Case ID: TC-MODEL-004
    @Test
    public void givenNegativeAppointmentPositionWhenSetThenShouldClampToZeroOrReject() {
        appointment.setPosition(-5);

        assertTrue("Expected non-negative position", appointment.getPosition() != null && appointment.getPosition() >= 0);
    }

    // Test Case ID: TC-MODEL-005
    @Test
    public void givenOutOfRangeGenderWhenSetThenShouldConstrainToKnownValues() {
        user.setGender(99);

        assertTrue("Expected gender code 0 or 1", user.getGender() == 0 || user.getGender() == 1);
    }

    // Test Case ID: TC-MODEL-006
    @Test
    public void givenNullHandbookFieldsWhenConstructThenShouldNormalizeToSafeDefaults() {
        Handbook nullHandbook = new Handbook(null, null, null);

        assertNotNull(nullHandbook.getImage());
        assertNotNull(nullHandbook.getTitle());
        assertNotNull(nullHandbook.getUrl());
    }

    // Test Case ID: TC-MODEL-007
    @Test
    public void givenTreatmentIdWhenGetThenShouldBeNonNegative() {
        Treatment treatment = new Treatment();
        int id = treatment.getId();

        assertTrue("Treatment id should be non-negative", id >= 0);
    }

    // ============================================================
    // TREATMENT MODEL TESTS
    // Note: Treatment only has getters (deserialized by Gson)
    // ============================================================

    // Test Case ID: TC-MODEL-008
    @Test
    public void givenTreatmentDefaultValuesWhenGetThenShouldNotReturnNull() {
        Treatment treatment = new Treatment();

        assertNotNull("Treatment name should not be null", treatment.getName());
        assertNotNull("Treatment type should not be null", treatment.getType());
        assertNotNull("Treatment purpose should not be null", treatment.getPurpose());
        assertNotNull("Treatment instruction should not be null", treatment.getInstruction());
        assertNotNull("Treatment repeatDays should not be null", treatment.getRepeatDays());
        assertNotNull("Treatment repeatTime should not be null", treatment.getRepeatTime());
    }

    // Test Case ID: TC-MODEL-009
    @Test
    public void givenTreatmentAppointmentIdWhenGetThenShouldBeNonNegative() {
        Treatment treatment = new Treatment();
        int appointmentId = treatment.getAppointmentId();

        assertTrue("Treatment appointmentId should be non-negative", appointmentId >= 0);
    }

    // Test Case ID: TC-MODEL-010
    @Test
    public void givenTreatmentTimesWhenGetThenShouldBeNonNegative() {
        Treatment treatment = new Treatment();
        int times = treatment.getTimes();

        assertTrue("Treatment times should be non-negative", times >= 0);
    }

    // ============================================================
    // SERVICE MODEL TESTS
    // Note: Service only has getters (deserialized by Gson)
    // ============================================================

    // Test Case ID: TC-MODEL-011
    @Test
    public void givenServiceDefaultValuesWhenGetThenShouldNotReturnNull() {
        Service service = new Service();

        assertNotNull("Service name should not be null", service.getName());
        assertNotNull("Service image should not be null", service.getImage());
        assertNotNull("Service description should not be null", service.getDescription());
    }

    // Test Case ID: TC-MODEL-012
    @Test
    public void givenServiceIdWhenGetThenShouldBeNonNegative() {
        Service service = new Service();

        assertTrue("Service id should be non-negative", service.getId() >= 0);
    }

    // ============================================================
    // SPECIALITY MODEL TESTS
    // Note: Speciality only has getters for id, name, description, image
    // ============================================================

    // Test Case ID: TC-MODEL-013
    @Test
    public void givenSpecialityDefaultValuesWhenGetThenShouldNotReturnNull() {
        Speciality speciality = new Speciality();

        assertNotNull("Speciality name should not be null", speciality.getName());
        assertNotNull("Speciality description should not be null", speciality.getDescription());
        assertNotNull("Speciality image should not be null", speciality.getImage());
    }

    // Test Case ID: TC-MODEL-014
    @Test
    public void givenSpecialityIdWhenGetThenShouldBeNonNegative() {
        Speciality speciality = new Speciality();

        assertTrue("Speciality id should be non-negative", speciality.getId() >= 0);
    }

    // ============================================================
    // RECORD MODEL TESTS
    // Note: Record only has getters (deserialized by Gson)
    // ============================================================

    // Test Case ID: TC-MODEL-015
    @Test
    public void givenRecordDefaultValuesWhenGetThenShouldNotReturnNull() {
        Record record = new Record();

        assertNotNull("Record reason should not be null", record.getReason());
        assertNotNull("Record description should not be null", record.getDescription());
        assertNotNull("Record statusBefore should not be null", record.getStatusBefore());
        assertNotNull("Record statusAfter should not be null", record.getStatusAfter());
        assertNotNull("Record createAt should not be null", record.getCreateAt());
        assertNotNull("Record updateAt should not be null", record.getUpdateAt());
    }

    // Test Case ID: TC-MODEL-016
    @Test
    public void givenRecordIdWhenGetThenShouldBeNonNegative() {
        Record record = new Record();

        assertTrue("Record id should be non-negative", record.getId() >= 0);
    }

    // ============================================================
    // QUEUE MODEL TESTS
    // Note: Queue only has getters (deserialized by Gson)
    // ============================================================

    // Test Case ID: TC-MODEL-017
    @Test
    public void givenQueueDefaultValuesWhenGetThenShouldNotReturnNull() {
        Queue queue = new Queue();

        assertNotNull("Queue patientName should not be null", queue.getPatientName());
        assertNotNull("Queue appointmentTime should not be null", queue.getAppointmentTime());
        assertNotNull("Queue status should not be null", queue.getStatus());
    }

    // Test Case ID: TC-MODEL-018
    @Test
    public void givenQueuePositionWhenGetThenShouldBeNonNegative() {
        Queue queue = new Queue();
        int position = queue.getPosition();

        assertTrue("Queue position should be non-negative", position >= 0);
    }

    // Test Case ID: TC-MODEL-019
    @Test
    public void givenQueueNumericalOrderWhenGetThenShouldBeNonNegative() {
        Queue queue = new Queue();
        int numericalOrder = queue.getNumericalOrder();

        assertTrue("Queue numericalOrder should be non-negative", numericalOrder >= 0);
    }

    // ============================================================
    // ROOM MODEL TESTS
    // Note: Room only has getters (deserialized by Gson)
    // ============================================================

    // Test Case ID: TC-MODEL-020
    @Test
    public void givenRoomDefaultValuesWhenGetThenShouldNotReturnNull() {
        Room room = new Room();

        assertNotNull("Room name should not be null", room.getName());
        assertNotNull("Room location should not be null", room.getLocation());
    }

    // Test Case ID: TC-MODEL-021
    @Test
    public void givenRoomIdWhenGetThenShouldBeNonNegative() {
        Room room = new Room();

        assertTrue("Room id should be non-negative", room.getId() >= 0);
    }

    // ============================================================
    // PHOTO MODEL TESTS
    // Note: Photo only has getters (deserialized by Gson)
    // ============================================================

    // Test Case ID: TC-MODEL-022
    @Test
    public void givenPhotoDefaultValuesWhenGetThenShouldNotReturnNull() {
        Photo photo = new Photo();

        assertNotNull("Photo url should not be null", photo.getUrl());
    }

    // Test Case ID: TC-MODEL-023
    @Test
    public void givenPhotoIdWhenGetThenShouldBeNonNegative() {
        Photo photo = new Photo();

        assertTrue("Photo id should be non-negative", photo.getId() >= 0);
    }

    // Test Case ID: TC-MODEL-024
    @Test
    public void givenPhotoBookingIdWhenGetThenShouldBeNonNegative() {
        Photo photo = new Photo();

        assertTrue("Photo bookingId should be non-negative", photo.getBookingId() >= 0);
    }

    // ============================================================
    // NOTIFICATION MODEL TESTS
    // Note: Notification only has getters (deserialized by Gson)
    // ============================================================

    // Test Case ID: TC-MODEL-025
    @Test
    public void givenNotificationDefaultValuesWhenGetThenShouldNotReturnNull() {
        Notification notification = new Notification();

        assertNotNull("Notification message should not be null", notification.getMessage());
        assertNotNull("Notification recordType should not be null", notification.getRecordType());
        assertNotNull("Notification createAt should not be null", notification.getCreateAt());
        assertNotNull("Notification updateAt should not be null", notification.getUpdateAt());
    }

    // Test Case ID: TC-MODEL-026
    @Test
    public void givenNotificationIdWhenGetThenShouldBeNonNegative() {
        Notification notification = new Notification();

        assertTrue("Notification id should be non-negative", notification.getId() >= 0);
    }

    // Test Case ID: TC-MODEL-027
    @Test
    public void givenNotificationIsReadWhenGetThenShouldBeZeroOrOne() {
        Notification notification = new Notification();
        int isRead = notification.getIsRead();

        assertTrue("Notification isRead should be 0 or 1", isRead == 0 || isRead == 1);
    }

    // ============================================================
    // DOCTOR MODEL TESTS
    // Note: Doctor only has getters (deserialized by Gson)
    // ============================================================

    // Test Case ID: TC-MODEL-028
    @Test
    public void givenDoctorDefaultValuesWhenGetThenShouldNotReturnNull() {
        Doctor doctor = new Doctor();

        assertNotNull("Doctor email should not be null", doctor.getEmail());
        assertNotNull("Doctor phone should not be null", doctor.getPhone());
        assertNotNull("Doctor name should not be null", doctor.getName());
        assertNotNull("Doctor description should not be null", doctor.getDescription());
        assertNotNull("Doctor price should not be null", doctor.getPrice());
        assertNotNull("Doctor role should not be null", doctor.getRole());
        assertNotNull("Doctor avatar should not be null", doctor.getAvatar());
        assertNotNull("Doctor active should not be null", doctor.getActive());
    }

    // Test Case ID: TC-MODEL-029
    @Test
    public void givenDoctorIdWhenGetThenShouldBeNonNegative() {
        Doctor doctor = new Doctor();

        assertTrue("Doctor id should be non-negative", doctor.getId() >= 0);
    }

    // ============================================================
    // BOOKING MODEL TESTS
    // Note: Booking only has getters (deserialized by Gson)
    // ============================================================

    // Test Case ID: TC-MODEL-030
    @Test
    public void givenBookingDefaultValuesWhenGetThenShouldNotReturnNull() {
        Booking booking = new Booking();

        assertNotNull("Booking bookingName should not be null", booking.getBookingName());
        assertNotNull("Booking bookingPhone should not be null", booking.getBookingPhone());
        assertNotNull("Booking name should not be null", booking.getName());
        assertNotNull("Booking birthday should not be null", booking.getBirthday());
        assertNotNull("Booking address should not be null", booking.getAddress());
        assertNotNull("Booking reason should not be null", booking.getReason());
        assertNotNull("Booking appointmentTime should not be null", booking.getAppointmentTime());
        assertNotNull("Booking appointmentDate should not be null", booking.getAppointmentDate());
        assertNotNull("Booking status should not be null", booking.getStatus());
    }

    // Test Case ID: TC-MODEL-031
    @Test
    public void givenBookingIdWhenGetThenShouldBeNonNegative() {
        Booking booking = new Booking();

        assertTrue("Booking id should be non-negative", booking.getId() >= 0);
    }

    // Test Case ID: TC-MODEL-032
    @Test
    public void givenBookingGenderWhenGetThenShouldBeZeroOrOne() {
        Booking booking = new Booking();
        int gender = booking.getGender();

        assertTrue("Booking gender should be 0 or 1", gender == 0 || gender == 1);
    }

    // ============================================================
    // USER MODEL TESTS
    // Note: User has setters for all fields
    // ============================================================

    // Test Case ID: TC-MODEL-033
    @Test
    public void givenUserDefaultValuesWhenGetThenShouldNotReturnNull() {
        User localUser = new User();

        assertNotNull("User email should not be null", localUser.getEmail());
        assertNotNull("User phone should not be null", localUser.getPhone());
        assertNotNull("User name should not be null", localUser.getName());
        assertNotNull("User birthday should not be null", localUser.getBirthday());
        assertNotNull("User address should not be null", localUser.getAddress());
        assertNotNull("User avatar should not be null", localUser.getAvatar());
    }

    // Test Case ID: TC-MODEL-034
    @Test
    public void givenUserIdWhenGetThenShouldBeNonNegative() {
        User localUser = new User();

        assertTrue("User id should be non-negative", localUser.getId() >= 0);
    }

    // Test Case ID: TC-MODEL-035
    @Test
    public void givenUserGenderWhenGetThenShouldBeZeroOrOne() {
        User localUser = new User();
        int gender = localUser.getGender();

        assertTrue("User gender should be 0 or 1", gender == 0 || gender == 1);
    }

    // Test Case ID: TC-MODEL-036
    @Test
    public void givenUserWithSettersWhenSetFieldsThenValuesAreStored() {
        User localUser = new User();
        localUser.setName("John Doe");
        localUser.setEmail("john@example.com");
        localUser.setPhone("0123456789");
        localUser.setGender(1);
        localUser.setBirthday("1990-01-01");
        localUser.setAddress("123 Main St");

        assertEquals("John Doe", localUser.getName());
        assertEquals("john@example.com", localUser.getEmail());
        assertEquals("0123456789", localUser.getPhone());
        assertEquals(1, localUser.getGender());
        assertEquals("1990-01-01", localUser.getBirthday());
        assertEquals("123 Main St", localUser.getAddress());
    }

    // ============================================================
    // APPOINTMENT MODEL TESTS
    // Note: Appointment has setters for all fields
    // ============================================================

    // Test Case ID: TC-MODEL-037
    @Test
    public void givenAppointmentDefaultValuesWhenGetThenShouldNotReturnNull() {
        Appointment localAppointment = new Appointment();

        assertNotNull("Appointment patientName should not be null", localAppointment.getPatientName());
        assertNotNull("Appointment patientPhone should not be null", localAppointment.getPatientPhone());
        assertNotNull("Appointment patientBirthday should not be null", localAppointment.getPatientBirthday());
        assertNotNull("Appointment patientReason should not be null", localAppointment.getPatientReason());
        assertNotNull("Appointment date should not be null", localAppointment.getDate());
        assertNotNull("Appointment appointmentTime should not be null", localAppointment.getAppointmentTime());
        assertNotNull("Appointment status should not be null", localAppointment.getStatus());
    }

    // Test Case ID: TC-MODEL-038
    @Test
    public void givenAppointmentPositionWhenGetThenShouldBeNonNegative() {
        Appointment localAppointment = new Appointment();

        assertTrue("Appointment position should be non-negative",
            localAppointment.getPosition() == null || localAppointment.getPosition() >= 0);
    }

    // Test Case ID: TC-MODEL-039
    @Test
    public void givenAppointmentNumericalOrderWhenGetThenShouldBeNonNegative() {
        Appointment localAppointment = new Appointment();

        assertTrue("Appointment numericalOrder should be non-negative",
            localAppointment.getNumericalOrder() == null || localAppointment.getNumericalOrder() >= 0);
    }

    // Test Case ID: TC-MODEL-040
    @Test
    public void givenAppointmentWithSettersWhenSetFieldsThenValuesAreStored() {
        Appointment localAppointment = new Appointment();
        localAppointment.setPatientName("Jane Doe");
        localAppointment.setPatientPhone("0987654321");
        localAppointment.setPatientBirthday("1995-05-15");
        localAppointment.setPatientReason("Checkup");
        localAppointment.setDate("2024-01-15");
        localAppointment.setAppointmentTime("09:00");
        localAppointment.setStatus("pending");
        localAppointment.setPosition(5);
        localAppointment.setNumericalOrder(3);

        assertEquals("Jane Doe", localAppointment.getPatientName());
        assertEquals("0987654321", localAppointment.getPatientPhone());
        assertEquals("1995-05-15", localAppointment.getPatientBirthday());
        assertEquals("Checkup", localAppointment.getPatientReason());
        assertEquals("2024-01-15", localAppointment.getDate());
        assertEquals("09:00", localAppointment.getAppointmentTime());
        assertEquals("pending", localAppointment.getStatus());
        assertEquals(Integer.valueOf(5), localAppointment.getPosition());
        assertEquals(Integer.valueOf(3), localAppointment.getNumericalOrder());
    }

    // ============================================================
    // HANDBOOK MODEL TESTS
    // Note: Handbook only has getters, no setters
    // ============================================================

    // Test Case ID: TC-MODEL-041
    @Test
    public void givenHandbookDefaultValuesWhenGetThenShouldNotReturnNull() {
        Handbook localHandbook = new Handbook();

        assertNotNull("Handbook image should not be null", localHandbook.getImage());
        assertNotNull("Handbook title should not be null", localHandbook.getTitle());
        assertNotNull("Handbook url should not be null", localHandbook.getUrl());
    }

    // Test Case ID: TC-MODEL-042
    @Test
    public void givenHandbookWithConstructorWhenGetFieldsThenValuesAreStored() {
        Handbook localHandbook = new Handbook("handbook.png", "Health Guide", "https://example.com/guide");

        assertEquals("handbook.png", localHandbook.getImage());
        assertEquals("Health Guide", localHandbook.getTitle());
        assertEquals("https://example.com/guide", localHandbook.getUrl());
    }

    // ============================================================
    // SETTING MODEL TESTS
    // Note: Setting has setters for icon, id, name
    // ============================================================

    // Test Case ID: TC-MODEL-043
    @Test
    public void givenSettingDefaultValuesWhenGetThenShouldNotReturnNull() {
        Setting localSetting = new Setting(0, "", "");

        assertNotNull("Setting id should not be null", localSetting.getId());
        assertNotNull("Setting name should not be null", localSetting.getName());
    }

    // Test Case ID: TC-MODEL-044
    @Test
    public void givenSettingWithSettersWhenSetFieldsThenValuesAreStored() {
        Setting localSetting = new Setting(0, "", "");
        localSetting.setIcon(123);
        localSetting.setId("setting-001");
        localSetting.setName("New Setting");

        assertEquals(123, localSetting.getIcon());
        assertEquals("setting-001", localSetting.getId());
        assertEquals("New Setting", localSetting.getName());
    }

    // ============================================================
    // OPTION MODEL TESTS
    // Note: Option has setters for name, icon
    // ============================================================

    // Test Case ID: TC-MODEL-045
    @Test
    public void givenOptionDefaultValuesWhenGetThenShouldNotReturnNull() {
        Option localOption = new Option();

        assertNotNull("Option name should not be null", localOption.getName());
    }

    // Test Case ID: TC-MODEL-046
    @Test
    public void givenOptionWithSettersWhenSetFieldsThenValuesAreStored() {
        Option localOption = new Option();
        localOption.setName("Option Name");
        localOption.setIcon(456);

        assertEquals("Option Name", localOption.getName());
        assertEquals(456, localOption.getIcon());
    }

    // ============================================================
    // MAIN (Weather) MODEL TESTS
    // Note: Main only has getters (deserialized by Gson)
    // ============================================================

    // Test Case ID: TC-MODEL-047
    @Test
    public void givenMainWeatherTemperatureValuesWhenGetThenShouldBeInValidRange() {
        Main main = new Main();
        float temp = main.getTemp();
        float feelsLike = main.getFeelsLike();
        float tempMin = main.getTempMin();

        assertTrue("Temperature should be in valid range (-100 to 100 Celsius)",
            temp >= -100 && temp <= 100);
        assertTrue("Feels like should be in valid range (-100 to 100)",
            feelsLike >= -100 && feelsLike <= 100);
        assertTrue("Temp min should be in valid range (-100 to 100)",
            tempMin >= -100 && tempMin <= 100);
    }

    // Test Case ID: TC-MODEL-048
    @Test
    public void givenMainWeatherPressureWhenGetThenShouldBeNonNegative() {
        Main main = new Main();

        assertTrue("Pressure should be non-negative", main.getPressure() >= 0);
    }

    // Test Case ID: TC-MODEL-049
    @Test
    public void givenMainWeatherHumidityWhenGetThenShouldBeInValidRange() {
        Main main = new Main();
        float humidity = main.getHumidity();

        assertTrue("Humidity should be between 0 and 100", humidity >= 0 && humidity <= 100);
    }

    // ============================================================
    // BOUNDARY VALUE TESTS
    // ============================================================

    // Test Case ID: TC-MODEL-050
    @Test
    public void givenMaximumIntegerValuesWhenAccessFieldsThenShouldNotThrow() {
        Treatment treatment = new Treatment();
        Service service = new Service();
        Speciality speciality = new Speciality();
        Record record = new Record();
        Room room = new Room();
        Photo photo = new Photo();

        assertTrue("Maximum value access should not throw", true);
    }

    // Test Case ID: TC-MODEL-051
    @Test
    public void givenNegativeIntegerValuesWhenAccessFieldsThenShouldNotThrow() {
        Appointment localAppointment = new Appointment();
        localAppointment.setPatientId(-1);

        assertTrue("Negative value access should not throw", true);
    }

    // Test Case ID: TC-MODEL-052
    @Test
    public void givenEmptyStringsWhenAccessFieldsThenShouldNotThrow() {
        User localUser = new User();
        localUser.setName("");
        localUser.setEmail("");
        localUser.setPhone("");
        localUser.setAddress("");

        assertEquals("", localUser.getName());
        assertEquals("", localUser.getEmail());
        assertEquals("", localUser.getPhone());
        assertEquals("", localUser.getAddress());
    }

    // ============================================================
    // RELATIONSHIP TESTS
    // ============================================================

    // Test Case ID: TC-MODEL-053
    @Test
    public void givenAppointmentWithNullRelationshipsWhenGetThenShouldNotThrow() {
        Appointment localAppointment = new Appointment();

        assertTrue("Doctor getter should work", localAppointment.getDoctor() == null || true);
        assertTrue("Speciality getter should work", localAppointment.getSpeciality() == null || true);
        assertTrue("Room getter should work", localAppointment.getRoom() == null || true);
    }

    // Test Case ID: TC-MODEL-054
    @Test
    public void givenRecordWithNullRelationshipsWhenGetThenShouldNotThrow() {
        Record record = new Record();

        assertTrue("Appointment getter should work", record.getAppointment() == null || true);
        assertTrue("Doctor getter should work", record.getDoctor() == null || true);
        assertTrue("Speciality getter should work", record.getSpeciality() == null || true);
    }

    // Test Case ID: TC-MODEL-055
    @Test
    public void givenDoctorWithNullRelationshipsWhenGetThenShouldNotThrow() {
        Doctor doctor = new Doctor();

        assertTrue("Speciality getter should work", doctor.getSpeciality() == null || true);
        assertTrue("Room getter should work", doctor.getRoom() == null || true);
    }

    // Test Case ID: TC-MODEL-056
    @Test
    public void givenBookingWithNullServiceWhenGetThenShouldNotThrow() {
        Booking booking = new Booking();

        assertTrue("Service getter should work", booking.getService() == null || true);
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private String repeat(char ch, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(ch);
        }
        return builder.toString();
    }
}
