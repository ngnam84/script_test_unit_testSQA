package com.example.do_an_tot_nghiep.Container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.do_an_tot_nghiep.Model.Appointment;
import com.example.do_an_tot_nghiep.Model.Booking;
import com.example.do_an_tot_nghiep.Model.Doctor;
import com.example.do_an_tot_nghiep.Model.Notification;
import com.example.do_an_tot_nghiep.Model.Service;
import com.example.do_an_tot_nghiep.Model.Speciality;
import com.example.do_an_tot_nghiep.Model.Treatment;
import com.example.do_an_tot_nghiep.Model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Test;



public class ContainerModelsTest {

    private Gson gson;

    @Before
    public void setUp() {
        gson = new GsonBuilder().setLenient().create();
    }

    // ========================================================================
    // 1. WEATHER FORECAST TESTS
    // ========================================================================

    // TC-CONTAINER-017 – wrong timezone value
    @Test
    public void TC_CONTAINER_017_wrongTimezoneValue() {
        String json = "{" +
                "\"timezone\":25200," +
                "\"id\":1581123," +
                "\"name\":\"Da Nang\"," +
                "\"cod\":\"200\"," +
                "\"main\":{\"temp\":27.5,\"feels_like\":29.0,\"temp_min\":26.0,\"pressure\":1008.0,\"humidity\":88.0}" +
                "}";

        WeatherForecast forecast = gson.fromJson(json, WeatherForecast.class);

        assertEquals("Wrong timezone should be detected", 99999, forecast.getTimeZone());
    }

    // TC-CONTAINER-001 – map nested object
    @Test
    public void TC_CONTAINER_001_mapNestedObject() {
        String json = "{" +
                "\"timezone\":25200," +
                "\"id\":1581123," +
                "\"name\":\"Da Nang\"," +
                "\"cod\":\"200\"," +
                "\"main\":{\"temp\":27.5,\"feels_like\":29.0,\"temp_min\":26.0,\"pressure\":1008.0,\"humidity\":88.0}" +
                "}";

        WeatherForecast forecast = gson.fromJson(json, WeatherForecast.class);

        assertEquals(25200, forecast.getTimeZone());
        assertEquals(1581123, forecast.getId());
        assertEquals("Da Nang", forecast.getName());
        assertEquals("200", forecast.getCod());
        assertNotNull(forecast.getMain());
        assertEquals(27.5f, forecast.getMain().getTemp(), 0.0001f);
        assertEquals(29.0f, forecast.getMain().getFeelsLike(), 0.0001f);
    }

    // ========================================================================
    // 2. EMPTY QUEUE TESTS
    // ========================================================================

    // TC-CONTAINER-018 – empty list handling
    @Test
    public void TC_CONTAINER_018_emptyListHandlingWrong() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"quantity\":0," +
                "\"data\":[]" +
                "}";

        AppointmentQueue queue = gson.fromJson(json, AppointmentQueue.class);

        assertEquals("List size should be 1 (wrong expectation)", 1, queue.getData().size());
    }

    // TC-CONTAINER-002 – empty list handling
    @Test
    public void TC_CONTAINER_002_emptyListHandlingCorrect() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"quantity\":0," +
                "\"data\":[]" +
                "}";

        AppointmentQueue queue = gson.fromJson(json, AppointmentQueue.class);

        assertEquals(1, queue.getResult());
        assertEquals(0, queue.getQuantity());
        assertNotNull(queue.getData());
        assertEquals("List should be empty", 0, queue.getData().size());
    }

    // ========================================================================
    // 3. APPOINTMENT LIST TESTS
    // ========================================================================

    // TC-CONTAINER-019 – wrong patient name
    @Test
    public void TC_CONTAINER_019_wrongPatientName() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"quantity\":1," +
                "\"data\":[{" +
                "\"id\":10," +
                "\"date\":\"2026-04-22\"," +
                "\"numerical_order\":2," +
                "\"position\":1," +
                "\"patient_id\":99," +
                "\"patient_name\":\"Nguyen Van A\"," +
                "\"patient_phone\":\"0123456789\"," +
                "\"patient_birthday\":\"2000-01-01\"," +
                "\"patient_reason\":\"Headache\"," +
                "\"appointment_time\":\"08:30\"," +
                "\"status\":\"pending\"," +
                "\"doctor\":{\"id\":7,\"email\":\"doctor@test.com\",\"phone\":\"0900000000\",\"name\":\"Dr One\",\"description\":\"General\",\"price\":\"500\",\"role\":\"doctor\",\"avatar\":\"avatar.png\",\"active\":\"1\",\"create_at\":\"2026-01-01\",\"update_at\":\"2026-01-02\"}," +
                "\"speciality\":{\"id\":2,\"name\":\"Cardio\",\"description\":\"Heart\",\"doctor_quantity\":4,\"image\":\"cardio.png\"}," +
                "\"room\":{\"id\":9,\"name\":\"Room 1\",\"location\":\"Floor 1\"}" +
                "}]" +
                "}";

        AppointmentReadAll response = gson.fromJson(json, AppointmentReadAll.class);
        Appointment appointment = response.getData().get(0);

        assertEquals("Wrong patient name should be detected", "Nguyen Van B", appointment.getPatientName());
    }

    // TC-CONTAINER-003 – nested object mapping
    @Test
    public void TC_CONTAINER_003_nestedObjectMapping() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"quantity\":1," +
                "\"data\":[{" +
                "\"id\":10," +
                "\"date\":\"2026-04-22\"," +
                "\"numerical_order\":2," +
                "\"position\":1," +
                "\"patient_id\":99," +
                "\"patient_name\":\"Nguyen Van A\"," +
                "\"patient_phone\":\"0123456789\"," +
                "\"patient_birthday\":\"2000-01-01\"," +
                "\"patient_reason\":\"Headache\"," +
                "\"appointment_time\":\"08:30\"," +
                "\"status\":\"pending\"," +
                "\"doctor\":{\"id\":7,\"email\":\"doctor@test.com\",\"phone\":\"0900000000\",\"name\":\"Dr One\",\"description\":\"General\",\"price\":\"500\",\"role\":\"doctor\",\"avatar\":\"avatar.png\",\"active\":\"1\",\"create_at\":\"2026-01-01\",\"update_at\":\"2026-01-02\",\"speciality\":{\"id\":2,\"name\":\"Cardio\",\"description\":\"Heart\",\"doctor_quantity\":4,\"image\":\"cardio.png\"},\"room\":{\"id\":9,\"name\":\"Room 1\",\"location\":\"Floor 1\"}}," +
                "\"speciality\":{\"id\":2,\"name\":\"Cardio\",\"description\":\"Heart\",\"doctor_quantity\":4,\"image\":\"cardio.png\"}," +
                "\"room\":{\"id\":9,\"name\":\"Room 1\",\"location\":\"Floor 1\"}" +
                "}]" +
                "}";

        AppointmentReadAll response = gson.fromJson(json, AppointmentReadAll.class);
        Appointment appointment = response.getData().get(0);

        assertEquals(1, response.getResult());
        assertEquals(1, response.getQuantity());
        assertEquals("ok", response.getMsg());
        assertEquals(1, response.getData().size());
        assertEquals("Nguyen Van A", appointment.getPatientName());
        assertNotNull(appointment.getDoctor());
        assertNotNull(appointment.getSpeciality());
        assertNotNull(appointment.getRoom());
        assertEquals("Dr One", appointment.getDoctor().getName());
        assertEquals("Room 1", appointment.getRoom().getName());
    }

    // ========================================================================
    // 4. APPOINTMENT DETAIL TESTS
    // ========================================================================

    // TC-CONTAINER-020 – wrong null check
    @Test
    public void TC_CONTAINER_020_wrongNullCheck() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"data\":{" +
                "\"id\":10,\"date\":\"2026-04-22\",\"numerical_order\":2,\"position\":1,\"patient_id\":99,\"patient_name\":\"Nguyen Van A\",\"patient_phone\":\"0123456789\",\"patient_birthday\":\"2000-01-01\",\"patient_reason\":\"Headache\",\"appointment_time\":\"08:30\",\"status\":\"pending\",\"doctor\":{\"id\":7,\"email\":\"doctor@test.com\",\"phone\":\"0900000000\",\"name\":\"Dr One\",\"description\":\"General\",\"price\":\"500\",\"role\":\"doctor\",\"avatar\":\"avatar.png\",\"active\":\"1\",\"create_at\":\"2026-01-01\",\"update_at\":\"2026-01-02\"},\"speciality\":{\"id\":2,\"name\":\"Cardio\",\"description\":\"Heart\",\"doctor_quantity\":4,\"image\":\"cardio.png\"},\"room\":{\"id\":9,\"name\":\"Room 1\",\"location\":\"Floor 1\"}" +
                "}" +
                "}";

        AppointmentReadByID response = gson.fromJson(json, AppointmentReadByID.class);

        assertNull("Data should be null (wrong expectation)", response.getData());
    }

    // TC-CONTAINER-004 – readById mapping
    @Test
    public void TC_CONTAINER_004_readByIdMapping() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"data\":{" +
                "\"id\":10,\"date\":\"2026-04-22\",\"numerical_order\":2,\"position\":1,\"patient_id\":99,\"patient_name\":\"Nguyen Van A\",\"patient_phone\":\"0123456789\",\"patient_birthday\":\"2000-01-01\",\"patient_reason\":\"Headache\",\"appointment_time\":\"08:30\",\"status\":\"pending\",\"doctor\":{\"id\":7,\"email\":\"doctor@test.com\",\"phone\":\"0900000000\",\"name\":\"Dr One\",\"description\":\"General\",\"price\":\"500\",\"role\":\"doctor\",\"avatar\":\"avatar.png\",\"active\":\"1\",\"create_at\":\"2026-01-01\",\"update_at\":\"2026-01-02\"},\"speciality\":{\"id\":2,\"name\":\"Cardio\",\"description\":\"Heart\",\"doctor_quantity\":4,\"image\":\"cardio.png\"},\"room\":{\"id\":9,\"name\":\"Room 1\",\"location\":\"Floor 1\"}" +
                "}" +
                "}";

        AppointmentReadByID response = gson.fromJson(json, AppointmentReadByID.class);

        assertEquals(1, response.getResult());
        assertEquals("ok", response.getMsg());
        assertNotNull(response.getData());
        assertEquals("Nguyen Van A", response.getData().getPatientName());
    }

    // ========================================================================
    // 5. BOOKING LIST TESTS
    // ========================================================================

    // TC-CONTAINER-021 – wrong service name
    @Test
    public void TC_CONTAINER_021_wrongServiceName() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"quantity\":1," +
                "\"data\":[{" +
                "\"id\":5,\"booking_name\":\"Nguyen Van A\",\"booking_phone\":\"0123456789\",\"name\":\"Patient A\",\"gender\":1,\"birthday\":\"2000-01-01\",\"address\":\"Ha Noi\",\"reason\":\"Checkup\",\"appointment_time\":\"08:30\",\"appointment_date\":\"2026-04-30\",\"status\":\"pending\",\"create_at\":\"2026-04-22\",\"update_at\":\"2026-04-22\",\"service\":{\"id\":3,\"name\":\"General Check\",\"image\":\"service.png\",\"description\":\"General exam\"}" +
                "}]" +
                "}";

        BookingReadAll response = gson.fromJson(json, BookingReadAll.class);
        Booking booking = response.getData().get(0);

        assertEquals("Wrong service name should be detected", "Dental Check", booking.getService().getName());
    }

    // TC-CONTAINER-005 – booking mapping
    @Test
    public void TC_CONTAINER_005_bookingMapping() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"quantity\":1," +
                "\"data\":[{" +
                "\"id\":5,\"booking_name\":\"Nguyen Van A\",\"booking_phone\":\"0123456789\",\"name\":\"Patient A\",\"gender\":1,\"birthday\":\"2000-01-01\",\"address\":\"Ha Noi\",\"reason\":\"Checkup\",\"appointment_time\":\"08:30\",\"appointment_date\":\"2026-04-30\",\"status\":\"pending\",\"create_at\":\"2026-04-22\",\"update_at\":\"2026-04-22\",\"service\":{\"id\":3,\"name\":\"General Check\",\"image\":\"service.png\",\"description\":\"General exam\"}" +
                "}]" +
                "}";

        BookingReadAll response = gson.fromJson(json, BookingReadAll.class);
        Booking booking = response.getData().get(0);

        assertEquals(1, response.getQuantity());
        assertEquals("Nguyen Van A", booking.getBookingName());
        assertNotNull(booking.getService());
        assertEquals("General Check", booking.getService().getName());
    }

    // ========================================================================
    // 6. BOOKING DETAIL TESTS
    // ========================================================================

    // TC-CONTAINER-022 – wrong result value
    @Test
    public void TC_CONTAINER_022_wrongResultValue() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"data\":{\"id\":5,\"booking_name\":\"Nguyen Van A\",\"booking_phone\":\"0123456789\",\"name\":\"Patient A\",\"gender\":1,\"birthday\":\"2000-01-01\",\"address\":\"Ha Noi\",\"reason\":\"Checkup\",\"appointment_time\":\"08:30\",\"appointment_date\":\"2026-04-30\",\"status\":\"pending\",\"create_at\":\"2026-04-22\",\"update_at\":\"2026-04-22\",\"service\":{\"id\":3,\"name\":\"General Check\",\"image\":\"service.png\",\"description\":\"General exam\"}}" +
                "}";

        BookingReadByID response = gson.fromJson(json, BookingReadByID.class);

        assertEquals("Wrong result value should be detected", 0, response.getResult());
    }

    // TC-CONTAINER-006 – booking detail mapping
    @Test
    public void TC_CONTAINER_006_bookingDetailMapping() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"data\":{\"id\":5,\"booking_name\":\"Nguyen Van A\",\"booking_phone\":\"0123456789\",\"name\":\"Patient A\",\"gender\":1,\"birthday\":\"2000-01-01\",\"address\":\"Ha Noi\",\"reason\":\"Checkup\",\"appointment_time\":\"08:30\",\"appointment_date\":\"2026-04-30\",\"status\":\"pending\",\"create_at\":\"2026-04-22\",\"update_at\":\"2026-04-22\",\"service\":{\"id\":3,\"name\":\"General Check\",\"image\":\"service.png\",\"description\":\"General exam\"}}" +
                "}";

        BookingReadByID response = gson.fromJson(json, BookingReadByID.class);

        assertNotNull(response.getData());
        assertEquals("Patient A", response.getData().getName());
        assertEquals("General Check", response.getData().getService().getName());
    }

    // ========================================================================
    // 7. BOOKING PHOTO TESTS
    // ========================================================================

    // TC-CONTAINER-023 – wrong quantity
    @Test
    public void TC_CONTAINER_023_wrongQuantity() {
        String json = "{" +
                "\"result\":1," +
                "\"quantity\":2," +
                "\"msg\":\"ok\"," +
                "\"booking\":{\"id\":5,\"booking_name\":\"Nguyen Van A\",\"booking_phone\":\"0123456789\",\"name\":\"Patient A\",\"gender\":1,\"birthday\":\"2000-01-01\",\"address\":\"Ha Noi\",\"reason\":\"Checkup\",\"appointment_time\":\"08:30\",\"appointment_date\":\"2026-04-30\",\"status\":\"pending\"}," +
                "\"data\":[{" +
                "\"id\":1,\"booking_id\":5,\"url\":\"https://example.com/1.jpg\"},{\"id\":2,\"booking_id\":5,\"url\":\"https://example.com/2.jpg\"}]" +
                "}";

        BookingPhotoReadAll response = gson.fromJson(json, BookingPhotoReadAll.class);

        assertEquals("Wrong quantity should be detected", 5, response.getQuantity());
    }

    // TC-CONTAINER-007 – photo mapping
    @Test
    public void TC_CONTAINER_007_photoMapping() {
        String json = "{" +
                "\"result\":1," +
                "\"quantity\":2," +
                "\"msg\":\"ok\"," +
                "\"booking\":{\"id\":5,\"booking_name\":\"Nguyen Van A\",\"booking_phone\":\"0123456789\",\"name\":\"Patient A\",\"gender\":1,\"birthday\":\"2000-01-01\",\"address\":\"Ha Noi\",\"reason\":\"Checkup\",\"appointment_time\":\"08:30\",\"appointment_date\":\"2026-04-30\",\"status\":\"pending\"}," +
                "\"data\":[{" +
                "\"id\":1,\"booking_id\":5,\"url\":\"https://example.com/1.jpg\"},{\"id\":2,\"booking_id\":5,\"url\":\"https://example.com/2.jpg\"}]" +
                "}";

        BookingPhotoReadAll response = gson.fromJson(json, BookingPhotoReadAll.class);

        assertEquals(2, response.getQuantity());
        assertNotNull(response.getBooking());
        assertEquals("Nguyen Van A", response.getBooking().getBookingName());
        assertEquals(2, response.getData().size());
        assertEquals("https://example.com/1.jpg", response.getData().get(0).getUrl());
    }

    // ========================================================================
    // 8. LOGIN TESTS
    // ========================================================================

    // TC-CONTAINER-024 – wrong access token
    @Test
    public void TC_CONTAINER_024_wrongAccessToken() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"accessToken\":\"token-123\"," +
                "\"data\":{\"id\":1,\"email\":\"patient@test.com\",\"phone\":\"0123456789\",\"name\":\"Patient A\",\"gender\":1,\"birthday\":\"2000-01-01\",\"address\":\"Ha Noi\",\"avatar\":\"avatar.png\",\"create_at\":\"2026-04-22\",\"update_at\":\"2026-04-22\"}" +
                "}";

        Login login = gson.fromJson(json, Login.class);

        assertEquals("Wrong access token should be detected", "token-999", login.getAccessToken());
    }

    // TC-CONTAINER-008 – login mapping
    @Test
    public void TC_CONTAINER_008_loginMapping() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"accessToken\":\"token-123\"," +
                "\"data\":{\"id\":1,\"email\":\"patient@test.com\",\"phone\":\"0123456789\",\"name\":\"Patient A\",\"gender\":1,\"birthday\":\"2000-01-01\",\"address\":\"Ha Noi\",\"avatar\":\"avatar.png\",\"create_at\":\"2026-04-22\",\"update_at\":\"2026-04-22\"}" +
                "}";

        Login login = gson.fromJson(json, Login.class);

        assertEquals(Integer.valueOf(1), login.getResult());
        assertEquals("token-123", login.getAccessToken());
        assertNotNull(login.getData());
        assertEquals("Patient A", login.getData().getName());
    }

    // ========================================================================
    // 9. PATIENT PROFILE TESTS
    // ========================================================================

    // TC-CONTAINER-025 – wrong email
    @Test
    public void TC_CONTAINER_025_wrongEmail() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"data\":{\"id\":1,\"email\":\"patient@test.com\",\"phone\":\"0123456789\",\"name\":\"Patient A\",\"gender\":1,\"birthday\":\"2000-01-01\",\"address\":\"Ha Noi\",\"avatar\":\"avatar.png\",\"create_at\":\"2026-04-22\",\"update_at\":\"2026-04-22\"}" +
                "}";

        PatientProfile profile = gson.fromJson(json, PatientProfile.class);

        assertEquals("Wrong email should be detected", "Anlex@gmali.com", profile.getData().getEmail());
    }

    // TC-CONTAINER-009 – profile mapping
    @Test
    public void TC_CONTAINER_009_profileMapping() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"data\":{\"id\":1,\"email\":\"patient@test.com\",\"phone\":\"0123456789\",\"name\":\"Patient A\",\"gender\":1,\"birthday\":\"2000-01-01\",\"address\":\"Ha Noi\",\"avatar\":\"avatar.png\",\"create_at\":\"2026-04-22\",\"update_at\":\"2026-04-22\"}" +
                "}";

        PatientProfile profile = gson.fromJson(json, PatientProfile.class);

        assertEquals(Integer.valueOf(1), profile.getResult());
        assertNotNull(profile.getData());
        assertEquals("patient@test.com", profile.getData().getEmail());
    }

    // ========================================================================
    // 10. AVATAR CHANGE TESTS
    // ========================================================================

    // TC-CONTAINER-026 – wrong avatar URL
    @Test
    public void TC_CONTAINER_026_wrongAvatarUrl() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"url\":\"https://example.com/avatar.png\"," +
                "\"data\":{\"id\":1,\"email\":\"patient@test.com\",\"phone\":\"0123456789\",\"name\":\"Patient A\",\"gender\":1,\"birthday\":\"2000-01-01\",\"address\":\"Ha Noi\",\"avatar\":\"avatar.png\",\"create_at\":\"2026-04-22\",\"update_at\":\"2026-04-22\"}" +
                "}";

        PatientProfileChangeAvatar response = gson.fromJson(json, PatientProfileChangeAvatar.class);

        assertEquals("Wrong avatar URL should be detected", "https:/abay.image.com/new-avatar.jpg", response.getUrl());
    }

    // TC-CONTAINER-010 – avatar mapping
    @Test
    public void TC_CONTAINER_010_avatarMapping() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"url\":\"https://example.com/avatar.png\"," +
                "\"data\":{\"id\":1,\"email\":\"patient@test.com\",\"phone\":\"0123456789\",\"name\":\"Patient A\",\"gender\":1,\"birthday\":\"2000-01-01\",\"address\":\"Ha Noi\",\"avatar\":\"avatar.png\",\"create_at\":\"2026-04-22\",\"update_at\":\"2026-04-22\"}" +
                "}";

        PatientProfileChangeAvatar response = gson.fromJson(json, PatientProfileChangeAvatar.class);

        assertEquals("https://example.com/avatar.png", response.getUrl());
        assertNotNull(response.getData());
        assertEquals("Patient A", response.getData().getName());
    }

    // ========================================================================
    // 11. NOTIFICATION LIST TESTS
    // ========================================================================

    // TC-CONTAINER-027 – wrong unread count
    @Test
    public void TC_CONTAINER_027_wrongUnreadCount() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"quantity\":1," +
                "\"quantityUnread\":1," +
                "\"data\":[{" +
                "\"id\":3,\"message\":\"New record\",\"record_id\":7,\"record_type\":\"appointment\",\"is_read\":0,\"create_at\":\"2026-04-22\",\"update_at\":\"2026-04-22\"}]" +
                "}";

        NotificationReadAll response = gson.fromJson(json, NotificationReadAll.class);

        assertEquals("Wrong unread count should be detected", 5, response.getQuantityUnread());
    }

    // TC-CONTAINER-011 – notification mapping
    @Test
    public void TC_CONTAINER_011_notificationMapping() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"quantity\":1," +
                "\"quantityUnread\":1," +
                "\"data\":[{" +
                "\"id\":3,\"message\":\"New record\",\"record_id\":7,\"record_type\":\"appointment\",\"is_read\":0,\"create_at\":\"2026-04-22\",\"update_at\":\"2026-04-22\"}]" +
                "}";

        NotificationReadAll response = gson.fromJson(json, NotificationReadAll.class);
        Notification notification = response.getData().get(0);

        assertEquals(1, response.getQuantityUnread());
        assertEquals("New record", notification.getMessage());
        assertEquals(0, notification.getIsRead());
    }

    // ========================================================================
    // 12. SERVICE LIST TESTS
    // ========================================================================

    // TC-CONTAINER-028 – wrong service name
    @Test
    public void TC_CONTAINER_028_wrongServiceName() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"quantity\":2," +
                "\"data\":[{" +
                "\"id\":1,\"name\":\"X-Ray\",\"image\":\"xray.png\",\"description\":\"Scan\"},{\"id\":2,\"name\":\"Ultrasound\",\"image\":\"us.png\",\"description\":\"Ultrasound scan\"}]" +
                "}";

        ServiceReadAll response = gson.fromJson(json, ServiceReadAll.class);

        assertEquals("Wrong service name should be detected", "MRL Scan", response.getData().get(1).getName());
    }

    // TC-CONTAINER-012 – service list
    @Test
    public void TC_CONTAINER_012_serviceList() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"quantity\":2," +
                "\"data\":[{" +
                "\"id\":1,\"name\":\"X-Ray\",\"image\":\"xray.png\",\"description\":\"Scan\"},{\"id\":2,\"name\":\"Ultrasound\",\"image\":\"us.png\",\"description\":\"Ultrasound scan\"}]" +
                "}";

        ServiceReadAll response = gson.fromJson(json, ServiceReadAll.class);

        assertEquals(2, response.getQuantity());
        assertEquals("X-Ray", response.getData().get(0).getName());
        assertEquals("Ultrasound", response.getData().get(1).getName());
    }

    // ========================================================================
    // 13. SPECIALITY LIST TESTS
    // ========================================================================

    // TC-CONTAINER-029 – wrong quantity
    @Test
    public void TC_CONTAINER_029_wrongQuantity() {
        String json = "{" +
                "\"result\":1," +
                "\"quantity\":1," +
                "\"data\":[{" +
                "\"id\":2,\"name\":\"Cardiology\",\"description\":\"Heart\",\"doctor_quantity\":4,\"image\":\"cardio.png\"}]" +
                "}";

        SpecialityReadAll response = gson.fromJson(json, SpecialityReadAll.class);

        assertEquals("Wrong quantity should be detected", 10, response.getQuantity());
    }

    // TC-CONTAINER-013 – speciality mapping
    @Test
    public void TC_CONTAINER_013_specialityMapping() {
        String json = "{" +
                "\"result\":1," +
                "\"quantity\":1," +
                "\"data\":[{" +
                "\"id\":2,\"name\":\"Cardiology\",\"description\":\"Heart\",\"doctor_quantity\":4,\"image\":\"cardio.png\"}]" +
                "}";

        SpecialityReadAll response = gson.fromJson(json, SpecialityReadAll.class);

        assertEquals(1, response.getQuantity());
        assertEquals("Cardiology", response.getData().get(0).getName());
    }

    // ========================================================================
    // 14. TREATMENT LIST TESTS
    // ========================================================================

    // TC-CONTAINER-030 – wrong treatment name
    @Test
    public void TC_CONTAINER_030_wrongTreatmentName() {
        String json = "{" +
                "\"result\":1," +
                "\"quantity\":1," +
                "\"data\":[{" +
                "\"id\":1,\"appointment_id\":9,\"name\":\"Aspirin\",\"type\":\"tablet\",\"times\":2,\"purpose\":\"Pain relief\",\"instruction\":\"After meal\",\"repeat_days\":\"5\",\"repeat_time\":\"08:00\"}]" +
                "}";

        TreatmentReadAll response = gson.fromJson(json, TreatmentReadAll.class);

        assertEquals("Wrong treatment name should be detected", "Ibuprofen", response.getData().get(0).getName());
    }

    // TC-CONTAINER-014 – treatment mapping
    @Test
    public void TC_CONTAINER_014_treatmentMapping() {
        String json = "{" +
                "\"result\":1," +
                "\"quantity\":1," +
                "\"data\":[{" +
                "\"id\":1,\"appointment_id\":9,\"name\":\"Aspirin\",\"type\":\"tablet\",\"times\":2,\"purpose\":\"Pain relief\",\"instruction\":\"After meal\",\"repeat_days\":\"5\",\"repeat_time\":\"08:00\"}]" +
                "}";

        TreatmentReadAll response = gson.fromJson(json, TreatmentReadAll.class);

        assertEquals(1, response.getQuantity());
        assertEquals("Aspirin", response.getData().get(0).getName());
        assertEquals(2, response.getData().get(0).getTimes());
    }

    // ========================================================================
    // 14B. SERVICE READ BY ID TESTS
    // ========================================================================

    // TC-CONTAINER-041 – wrong service id detection
    @Test
    public void TC_CONTAINER_041_wrongServiceId() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"data\":{\"id\":3,\"name\":\"MRI Scan\",\"image\":\"mri.png\",\"description\":\"Magnetic resonance imaging\"}" +
                "}";

        ServiceReadByID response = gson.fromJson(json, ServiceReadByID.class);

        assertEquals("Wrong service id should be detected", 5, response.getData().getId());
    }

    // TC-CONTAINER-042 – service read by id mapping
    @Test
    public void TC_CONTAINER_042_serviceReadByIdMapping() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"data\":{\"id\":3,\"name\":\"MRI Scan\",\"image\":\"mri.png\",\"description\":\"Magnetic resonance imaging\"}" +
                "}";

        ServiceReadByID response = gson.fromJson(json, ServiceReadByID.class);

        assertEquals(1, response.getResult());
        assertEquals("ok", response.getMsg());
        assertNotNull(response.getData());
        assertEquals(3, response.getData().getId());
        assertEquals("MRI Scan", response.getData().getName());
        assertEquals("mri.png", response.getData().getImage());
    }

    // ========================================================================
    // 14C. SPECIALITY READ BY ID TESTS
    // ========================================================================

    // TC-CONTAINER-043 – wrong speciality name detection
    @Test
    public void TC_CONTAINER_043_wrongSpecialityName() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"data\":{\"id\":2,\"name\":\"Neurology\",\"description\":\"Brain and nervous system\",\"doctor_quantity\":3,\"image\":\"neuro.png\"}" +
                "}";

        SpecialityReadByID response = gson.fromJson(json, SpecialityReadByID.class);

        assertEquals("Wrong speciality name should be detected", "Orthopedics", response.getData().getName());
    }

    // TC-CONTAINER-044 – speciality read by id mapping
    @Test
    public void TC_CONTAINER_044_specialityReadByIdMapping() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"data\":{\"id\":2,\"name\":\"Neurology\",\"description\":\"Brain and nervous system\",\"doctor_quantity\":3,\"image\":\"neuro.png\"}" +
                "}";

        SpecialityReadByID response = gson.fromJson(json, SpecialityReadByID.class);

        assertEquals(1, response.getResult());
        assertEquals("ok", response.getMsg());
        assertNotNull(response.getData());
        assertEquals(2, response.getData().getId());
        assertEquals("Neurology", response.getData().getName());
        assertEquals("Brain and nervous system", response.getData().getDescription());
        assertEquals("neuro.png", response.getData().getImage());
    }

    // ========================================================================
    // 14D. TREATMENT READ BY ID TESTS
    // ========================================================================

    // TC-CONTAINER-045 – wrong treatment type detection
    @Test
    public void TC_CONTAINER_045_wrongTreatmentType() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"data\":{\"id\":5,\"appointment_id\":10,\"name\":\"Vitamin D\",\"type\":\"capsule\",\"times\":1,\"purpose\":\"Supplement\",\"instruction\":\"Morning\",\"repeat_days\":\"30\",\"repeat_time\":\"07:00\"}" +
                "}";

        TreatmentReadByID response = gson.fromJson(json, TreatmentReadByID.class);

        assertEquals("Wrong treatment type should be detected", "tablet", response.getData().getType());
    }

    // TC-CONTAINER-046 – treatment read by id mapping
    @Test
    public void TC_CONTAINER_046_treatmentReadByIdMapping() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"data\":{\"id\":5,\"appointment_id\":10,\"name\":\"Vitamin D\",\"type\":\"capsule\",\"times\":1,\"purpose\":\"Supplement\",\"instruction\":\"Morning\",\"repeat_days\":\"30\",\"repeat_time\":\"07:00\"}" +
                "}";

        TreatmentReadByID response = gson.fromJson(json, TreatmentReadByID.class);

        assertEquals(1, response.getResult());
        assertEquals("ok", response.getMsg());
        assertNotNull(response.getData());
        assertEquals(5, response.getData().getId());
        assertEquals("Vitamin D", response.getData().getName());
        assertEquals("capsule", response.getData().getType());
        assertEquals(1, response.getData().getTimes());
    }

    // ========================================================================
    // 15. SIMPLE RESPONSE TESTS
    // ========================================================================

    // TC-CONTAINER-031 – wrong msg
    @Test
    public void TC_CONTAINER_031_wrongMsg() {
        String json = "{\"result\":1,\"msg\":\"cancelled\"}";

        BookingCancel bookingCancel = gson.fromJson(json, BookingCancel.class);

        assertEquals("Wrong msg should be detected", "failed", bookingCancel.getMsg());
    }

    // TC-CONTAINER-015 – scalar mapping
    @Test
    public void TC_CONTAINER_015_scalarMapping() {
        BookingCancel bookingCancel = gson.fromJson("{\"result\":1,\"msg\":\"cancelled\"}", BookingCancel.class);
        BookingPhotoDelete bookingPhotoDelete = gson.fromJson("{\"result\":1,\"msg\":\"deleted\"}", BookingPhotoDelete.class);
        BookingPhotoUpload bookingPhotoUpload = gson.fromJson("{\"result\":1,\"msg\":\"uploaded\",\"url\":\"https://example.com/p.png\"}", BookingPhotoUpload.class);
        NotificationCreate notificationCreate = gson.fromJson("{\"result\":1,\"msg\":\"created\"}", NotificationCreate.class);
        NotificationMarkAllAsRead markAllAsRead = gson.fromJson("{\"result\":1,\"msg\":\"ok\"}", NotificationMarkAllAsRead.class);
        NotificationMarkAsRead markAsRead = gson.fromJson("{\"result\":1,\"msg\":\"ok\"}", NotificationMarkAsRead.class);

        assertEquals(1, bookingCancel.getResult());
        assertEquals("cancelled", bookingCancel.getMsg());
        assertEquals("deleted", bookingPhotoDelete.getMsg());
        assertEquals("https://example.com/p.png", bookingPhotoUpload.getUrl());
        assertEquals("created", notificationCreate.getMsg());
        assertEquals("ok", markAllAsRead.getMsg());
        assertEquals("ok", markAsRead.getMsg());
    }

    // ========================================================================
    // 16. COMPLEX READ BY ID TESTS
    // ========================================================================

    // TC-CONTAINER-032 – wrong doctor name
    @Test
    public void TC_CONTAINER_032_wrongDoctorName() {
        String json = "{\"result\":1,\"msg\":\"ok\",\"data\":{\"id\":7,\"email\":\"doctor@test.com\",\"phone\":\"0900000000\",\"name\":\"Dr One\",\"description\":\"General\",\"price\":\"500\",\"role\":\"doctor\",\"avatar\":\"avatar.png\",\"active\":\"1\",\"create_at\":\"2026-04-22\",\"update_at\":\"2026-04-22\"}}";

        DoctorReadByID doctorReadByID = gson.fromJson(json, DoctorReadByID.class);

        assertEquals("Wrong doctor name should be detected", "Dr Two", doctorReadByID.getData().getName());
    }

    // TC-CONTAINER-016 – complex mapping
    @Test
    public void TC_CONTAINER_016_complexMapping() {
        String json = "{" +
                "\"result\":1,\"msg\":\"ok\"," +
                "\"data\":{\"id\":11,\"reason\":\"Checkup\",\"description\":\"Need follow-up\",\"status_before\":\"new\",\"status_after\":\"done\",\"create_at\":\"2026-04-22\",\"update_at\":\"2026-04-22\"," +
                "\"appointment\":{\"id\":10,\"date\":\"2026-04-22\",\"numerical_order\":2,\"position\":1,\"patient_id\":99,\"patient_name\":\"Nguyen Van A\",\"patient_phone\":\"0123456789\",\"patient_birthday\":\"2000-01-01\",\"patient_reason\":\"Headache\",\"appointment_time\":\"08:30\",\"status\":\"pending\"}," +
                "\"doctor\":{\"id\":7,\"email\":\"doctor@test.com\",\"phone\":\"0900000000\",\"name\":\"Dr One\",\"description\":\"General\",\"price\":\"500\",\"role\":\"doctor\",\"avatar\":\"avatar.png\",\"active\":\"1\",\"create_at\":\"2026-01-01\",\"update_at\":\"2026-01-02\"}," +
                "\"speciality\":{\"id\":2,\"name\":\"Cardiology\",\"description\":\"Heart\",\"doctor_quantity\":4,\"image\":\"cardio.png\"}}," +
                "\"record\":{\"id\":11,\"reason\":\"Checkup\",\"description\":\"Need follow-up\",\"status_before\":\"new\",\"status_after\":\"done\",\"create_at\":\"2026-04-22\",\"update_at\":\"2026-04-22\"}," +
                "\"service\":{\"id\":3,\"name\":\"X-Ray\",\"image\":\"xray.png\",\"description\":\"Scan\"}," +
                "\"treatment\":{\"id\":1,\"appointment_id\":9,\"name\":\"Aspirin\",\"type\":\"tablet\",\"times\":2,\"purpose\":\"Pain relief\",\"instruction\":\"After meal\",\"repeat_days\":\"5\",\"repeat_time\":\"08:00\"}}";

        RecordReadByID recordReadByID = gson.fromJson(json, RecordReadByID.class);

        assertNotNull(recordReadByID.getData());
        assertEquals("Checkup", recordReadByID.getData().getReason());
        assertNotNull(recordReadByID.getData().getAppointment());
        assertNotNull(recordReadByID.getData().getDoctor());
        assertNotNull(recordReadByID.getData().getSpeciality());
        assertEquals("Nguyen Van A", recordReadByID.getData().getAppointment().getPatientName());
        assertEquals("Dr One", recordReadByID.getData().getDoctor().getName());
    }

    // ========================================================================
    // 17. BOOKING CREATE TESTS
    // ========================================================================

    // TC-CONTAINER-036 – wrong booking name detection
    @Test
    public void TC_CONTAINER_036_wrongBookingName() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"Booking created successfully\"," +
                "\"data\":{\"id\":99,\"booking_name\":\"Nguyen Van B\",\"booking_phone\":\"0987654321\",\"name\":\"Patient B\",\"gender\":0,\"birthday\":\"1990-05-20\",\"address\":\"Ho Chi Minh\",\"reason\":\"Annual checkup\",\"appointment_time\":\"10:00\",\"appointment_date\":\"2026-05-10\",\"status\":\"pending\",\"service\":{\"id\":2,\"name\":\"Premium Checkup\",\"image\":\"premium.png\",\"description\":\"Full body checkup\"}}" +
                "}";

        BookingCreate response = gson.fromJson(json, BookingCreate.class);

        assertEquals("Wrong booking name should be detected", "Nguyen Van C", response.getData().getBookingName());
    }

    // TC-CONTAINER-037 – wrong booking status detection
    @Test
    public void TC_CONTAINER_037_wrongBookingStatus() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"Booking created successfully\"," +
                "\"data\":{\"id\":99,\"booking_name\":\"Nguyen Van B\",\"booking_phone\":\"0987654321\",\"name\":\"Patient B\",\"gender\":0,\"birthday\":\"1990-05-20\",\"address\":\"Ho Chi Minh\",\"reason\":\"Annual checkup\",\"appointment_time\":\"10:00\",\"appointment_date\":\"2026-05-10\",\"status\":\"pending\",\"service\":{\"id\":2,\"name\":\"Premium Checkup\",\"image\":\"premium.png\",\"description\":\"Full body checkup\"}}" +
                "}";

        BookingCreate response = gson.fromJson(json, BookingCreate.class);

        assertEquals("Wrong status should be detected", "confirmed", response.getData().getStatus());
    }

    // TC-CONTAINER-033 – booking create mapping
    @Test
    public void TC_CONTAINER_033_bookingCreateMapping() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"Booking created successfully\"," +
                "\"data\":{\"id\":99,\"booking_name\":\"Nguyen Van B\",\"booking_phone\":\"0987654321\",\"name\":\"Patient B\",\"gender\":0,\"birthday\":\"1990-05-20\",\"address\":\"Ho Chi Minh\",\"reason\":\"Annual checkup\",\"appointment_time\":\"10:00\",\"appointment_date\":\"2026-05-10\",\"status\":\"pending\",\"service\":{\"id\":2,\"name\":\"Premium Checkup\",\"image\":\"premium.png\",\"description\":\"Full body checkup\"}}" +
                "}";

        BookingCreate response = gson.fromJson(json, BookingCreate.class);

        assertEquals(1, response.getResult());
        assertEquals("Booking created successfully", response.getMsg());
        assertNotNull(response.getData());
        assertEquals("Nguyen Van B", response.getData().getBookingName());
        assertEquals("Patient B", response.getData().getName());
        assertEquals("pending", response.getData().getStatus());
    }

    // ========================================================================
    // 18. PROFILE UPDATE TESTS
    // ========================================================================

    // TC-CONTAINER-038 – wrong patient name detection
    @Test
    public void TC_CONTAINER_038_wrongPatientName() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"Profile updated successfully\"," +
                "\"data\":{\"id\":1,\"email\":\"patient@test.com\",\"phone\":\"0987654321\",\"name\":\"Patient Updated\",\"gender\":0,\"birthday\":\"1995-03-10\",\"address\":\"Can Tho\",\"avatar\":\"new_avatar.png\",\"create_at\":\"2026-04-22\",\"update_at\":\"2026-05-04\"}" +
                "}";

        PatientProfileChangePersonalInformation response = gson.fromJson(json, PatientProfileChangePersonalInformation.class);

        assertEquals("Wrong patient name should be detected", "Patient Wrong", response.getData().getName());
    }

    // TC-CONTAINER-039 – wrong phone number detection
    @Test
    public void TC_CONTAINER_039_wrongPhoneNumber() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"Profile updated successfully\"," +
                "\"data\":{\"id\":1,\"email\":\"patient@test.com\",\"phone\":\"0987654321\",\"name\":\"Patient Updated\",\"gender\":0,\"birthday\":\"1995-03-10\",\"address\":\"Can Tho\",\"avatar\":\"new_avatar.png\",\"create_at\":\"2026-04-22\",\"update_at\":\"2026-05-04\"}" +
                "}";

        PatientProfileChangePersonalInformation response = gson.fromJson(json, PatientProfileChangePersonalInformation.class);

        assertEquals("Wrong phone number should be detected", "0123456789", response.getData().getPhone());
    }

    // TC-CONTAINER-034 – profile update mapping
    @Test
    public void TC_CONTAINER_034_profileUpdateMapping() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"Profile updated successfully\"," +
                "\"data\":{\"id\":1,\"email\":\"patient@test.com\",\"phone\":\"0987654321\",\"name\":\"Patient Updated\",\"gender\":0,\"birthday\":\"1995-03-10\",\"address\":\"Can Tho\",\"avatar\":\"new_avatar.png\",\"create_at\":\"2026-04-22\",\"update_at\":\"2026-05-04\"}" +
                "}";

        PatientProfileChangePersonalInformation response = gson.fromJson(json, PatientProfileChangePersonalInformation.class);

        assertEquals(Integer.valueOf(1), response.getResult());
        assertEquals("Profile updated successfully", response.getMsg());
        assertNotNull(response.getData());
        assertEquals("Patient Updated", response.getData().getName());
        assertEquals("0987654321", response.getData().getPhone());
        assertEquals("Can Tho", response.getData().getAddress());
    }

    // ========================================================================
    // 19. DOCTOR LIST TESTS
    // ========================================================================

    // TC-CONTAINER-040 – wrong doctor quantity detection
    @Test
    public void TC_CONTAINER_040_wrongDoctorQuantity() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"quantity\":2," +
                "\"data\":[" +
                "{\"id\":1,\"email\":\"doctor1@test.com\",\"phone\":\"0900000001\",\"name\":\"Dr One\",\"description\":\"General physician\",\"price\":\"200\",\"role\":\"doctor\",\"avatar\":\"doc1.png\",\"active\":\"1\",\"create_at\":\"2026-01-01\",\"update_at\":\"2026-01-02\",\"speciality\":{\"id\":1,\"name\":\"General\",\"description\":\"General medicine\",\"doctor_quantity\":5,\"image\":\"general.png\"},\"room\":{\"id\":1,\"name\":\"Room 101\",\"location\":\"Floor 1\"}}," +
                "{\"id\":2,\"email\":\"doctor2@test.com\",\"phone\":\"0900000002\",\"name\":\"Dr Two\",\"description\":\"Heart specialist\",\"price\":\"500\",\"role\":\"doctor\",\"avatar\":\"doc2.png\",\"active\":\"1\",\"create_at\":\"2026-01-01\",\"update_at\":\"2026-01-02\",\"speciality\":{\"id\":2,\"name\":\"Cardiology\",\"description\":\"Heart care\",\"doctor_quantity\":3,\"image\":\"cardio.png\"},\"room\":{\"id\":2,\"name\":\"Room 202\",\"location\":\"Floor 2\"}}" +
                "]}";

        DoctorReadAll response = gson.fromJson(json, DoctorReadAll.class);

        assertEquals("Wrong quantity should be detected", 5, response.getQuantity());
    }

    // TC-CONTAINER-035 – doctor list with nested objects
    @Test
    public void TC_CONTAINER_035_doctorListWithNestedObjects() {
        String json = "{" +
                "\"result\":1," +
                "\"msg\":\"ok\"," +
                "\"quantity\":2," +
                "\"data\":[" +
                "{\"id\":1,\"email\":\"doctor1@test.com\",\"phone\":\"0900000001\",\"name\":\"Dr One\",\"description\":\"General physician\",\"price\":\"200\",\"role\":\"doctor\",\"avatar\":\"doc1.png\",\"active\":\"1\",\"create_at\":\"2026-01-01\",\"update_at\":\"2026-01-02\",\"speciality\":{\"id\":1,\"name\":\"General\",\"description\":\"General medicine\",\"doctor_quantity\":5,\"image\":\"general.png\"},\"room\":{\"id\":1,\"name\":\"Room 101\",\"location\":\"Floor 1\"}}," +
                "{\"id\":2,\"email\":\"doctor2@test.com\",\"phone\":\"0900000002\",\"name\":\"Dr Two\",\"description\":\"Heart specialist\",\"price\":\"500\",\"role\":\"doctor\",\"avatar\":\"doc2.png\",\"active\":\"1\",\"create_at\":\"2026-01-01\",\"update_at\":\"2026-01-02\",\"speciality\":{\"id\":2,\"name\":\"Cardiology\",\"description\":\"Heart care\",\"doctor_quantity\":3,\"image\":\"cardio.png\"},\"room\":{\"id\":2,\"name\":\"Room 202\",\"location\":\"Floor 2\"}}" +
                "]}";

        DoctorReadAll response = gson.fromJson(json, DoctorReadAll.class);

        assertEquals(1, response.getResult());
        assertEquals("ok", response.getMsg());
        assertEquals(2, response.getQuantity());
        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());
        assertEquals("Dr One", response.getData().get(0).getName());
        assertEquals("Dr Two", response.getData().get(1).getName());
        assertNotNull(response.getData().get(0).getSpeciality());
        assertNotNull(response.getData().get(0).getRoom());
        assertEquals("General", response.getData().get(0).getSpeciality().getName());
        assertEquals("Room 101", response.getData().get(0).getRoom().getName());
        assertEquals("Cardiology", response.getData().get(1).getSpeciality().getName());
        assertEquals("Room 202", response.getData().get(1).getRoom().getName());
    }
}
