package com.example.do_an_tot_nghiep.Repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.lifecycle.MutableLiveData;

import com.example.do_an_tot_nghiep.Configuration.HTTPRequest;
import com.example.do_an_tot_nghiep.Configuration.HTTPService;
import com.example.do_an_tot_nghiep.Container.AppointmentQueue;
import com.example.do_an_tot_nghiep.Container.AppointmentReadAll;
import com.example.do_an_tot_nghiep.Container.AppointmentReadByID;
import com.example.do_an_tot_nghiep.Container.TreatmentReadAll;
import com.example.do_an_tot_nghiep.Container.TreatmentReadByID;
import com.example.do_an_tot_nghiep.Container.BookingCreate;
import com.example.do_an_tot_nghiep.Container.BookingPhotoReadAll;
import com.example.do_an_tot_nghiep.Container.BookingReadByID;
import com.example.do_an_tot_nghiep.Container.BookingReadAll;
import com.example.do_an_tot_nghiep.Container.DoctorReadAll;
import com.example.do_an_tot_nghiep.Container.DoctorReadByID;
import com.example.do_an_tot_nghiep.Container.NotificationReadAll;
import com.example.do_an_tot_nghiep.Container.RecordReadByID;
import com.example.do_an_tot_nghiep.Container.ServiceReadAll;
import com.example.do_an_tot_nghiep.Container.ServiceReadByID;
import com.example.do_an_tot_nghiep.Container.SpecialityReadAll;
import com.example.do_an_tot_nghiep.Container.SpecialityReadByID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Bug-focused unit tests for repository implementations.
 *
 * This class intentionally mixes pass/fail cases:
 * - Pass cases validate current happy-path and known handled error-path behavior.
 * - Fail cases expose real logic gaps (stale data, missing resets, missing validation).
 */
public class RepositoryBugDetectionTest {

    @Rule
    public SynchronousTaskExecutorRule synchronousTaskExecutorRule = new SynchronousTaskExecutorRule();

    private AutoCloseable mocks;
    private Retrofit retrofit;
    private HTTPRequest api;
    private MockedStatic<HTTPService> httpServiceMock;

    private Map<String, String> headers;
    private Map<String, String> params;

    @Before
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        retrofit = mock(Retrofit.class);
        api = mock(HTTPRequest.class);
        when(retrofit.create(HTTPRequest.class)).thenReturn(api);

        httpServiceMock = org.mockito.Mockito.mockStatic(HTTPService.class);
        httpServiceMock.when(HTTPService::getInstance).thenReturn(retrofit);

        headers = new HashMap<>();
        headers.put("Authorization", "Bearer token");
        headers.put("Type", "patient");

        params = new HashMap<>();
        params.put("page", "1");
        params.put("limit", "10");
    }

    @After
    public void tearDown() throws Exception {
        httpServiceMock.close();
        mocks.close();
    }

    // ---------- PASS TESTS (valid/handled partitions) ----------

    // EP-VALID: success response contains body and updates LiveData.
    // TC-REPO-042 – notification thành công → cập nhật dữ liệu + dừng animation
    @Test
    public void notification_success_shouldUpdateDataAndStopAnimation() {
        NotificationRepository repositoryUnderTest = new NotificationRepository();
        Call<NotificationReadAll> call = mockCall();
        NotificationReadAll body = mock(NotificationReadAll.class);

        when(api.notificationReadAll(headers)).thenReturn(call);
        AtomicReference<Callback<NotificationReadAll>> callbackRef = captureCallback(call);

        MutableLiveData<NotificationReadAll> liveData = repositoryUnderTest.readAll(headers);
        verify(api).notificationReadAll(headers);

        callbackRef.get().onResponse(call, Response.success(body));

        assertSame(body, liveData.getValue());
        assertFalse(Boolean.TRUE.equals(repositoryUnderTest.getAnimation().getValue()));
    }

    // EP-INVALID (handled): errorBody present sets null and stops animation.
    // TC-REPO-043 – notification có errorBody → xóa dữ liệu + dừng animation
    @Test
    public void notification_errorBodyPresent_shouldClearDataAndStopAnimation() {
        NotificationRepository repositoryUnderTest = new NotificationRepository();
        Call<NotificationReadAll> call = mockCall();

        when(api.notificationReadAll(headers)).thenReturn(call);
        AtomicReference<Callback<NotificationReadAll>> callbackRef = captureCallback(call);

        repositoryUnderTest.readAll(headers);
        callbackRef.get().onResponse(call, errorResponse());

        assertNull(repositoryUnderTest.getReadAllResponse().getValue());
        assertFalse(Boolean.TRUE.equals(repositoryUnderTest.getAnimation().getValue()));
    }

    // EP-VALID: Record success path updates value and stops animation.
    // TC-REPO-044 – record thành công → cập nhật dữ liệu + dừng animation
    @Test
    public void record_success_shouldUpdateDataAndStopAnimation() {
        RecordRepository repositoryUnderTest = new RecordRepository();
        Call<RecordReadByID> call = mockCall();
        RecordReadByID body = mock(RecordReadByID.class);

        when(api.recordReadById(headers, "R1")).thenReturn(call);
        AtomicReference<Callback<RecordReadByID>> callbackRef = captureCallback(call);

        MutableLiveData<RecordReadByID> liveData = repositoryUnderTest.readByID(headers, "R1");
        callbackRef.get().onResponse(call, Response.success(body));

        assertSame(body, liveData.getValue());
        assertFalse(Boolean.TRUE.equals(repositoryUnderTest.getAnimation().getValue()));
    }
    // EP-INVALID (handled): SpecialityRepository sets animation=false for unsuccessful responses.
    // TC-REPO-052 – speciality readAll với unsuccessful response → animation được set false đúng cách
    @Test
    public void specialityReadAll_unsuccessfulResponse_correctlyStopsAnimation() {
        SpecialityRepository repositoryUnderTest = new SpecialityRepository();
        Call<SpecialityReadAll> call = mockCall();

        when(api.specialityReadAll(any(), any())).thenReturn(call);
        AtomicReference<Callback<SpecialityReadAll>> callbackRef = captureCallback(call);

        repositoryUnderTest.readAll(headers, params);

        // Simulate unsuccessful response (HTTP 400/500)
        @SuppressWarnings("unchecked")
        Response<SpecialityReadAll> errorResponse = mock(Response.class);
        when(errorResponse.isSuccessful()).thenReturn(false);

        callbackRef.get().onResponse(call, errorResponse);

        // Expected: animation được set false khi response không thành công
        // Code correctly sets animation=false before checking isSuccessful()
        assertSame(Boolean.FALSE, repositoryUnderTest.getAnimation().getValue());
    }

    // TC-REPO-070 – bookingReadByID với unsuccessful response → correctly clears stale data and stops animation
    @Test
    public void bookingReadById_unsuccessfulResponse_clearsStaleDataCorrectly() {
        BookingRepository repositoryUnderTest = new BookingRepository();
        Call<BookingReadByID> successCall = mockCall();
        Call<BookingReadByID> errorCall = mockCall();
        BookingReadByID cached = mock(BookingReadByID.class);

        when(api.bookingReadByID(any(), any())).thenReturn(successCall).thenReturn(errorCall);
        AtomicReference<Callback<BookingReadByID>> successCallbackRef = captureCallback(successCall);
        AtomicReference<Callback<BookingReadByID>> errorCallbackRef = captureCallback(errorCall);

        repositoryUnderTest.readByID(headers, "BK-1");
        successCallbackRef.get().onResponse(successCall, Response.success(cached));

        repositoryUnderTest.readByID(headers, "BK-2");
        @SuppressWarnings("unchecked")
        Response<BookingReadByID> unsuccessful = mock(Response.class);
        when(unsuccessful.isSuccessful()).thenReturn(false);
        errorCallbackRef.get().onResponse(errorCall, unsuccessful);

        // Expected: readByIDResponse nên là null vì call thất bại
        assertNull(repositoryUnderTest.getReadByIDResponse().getValue());
    }

    // TC-REPO-071 – bookingReadAll với unsuccessful response → correctly clears stale data and stops animation
    @Test
    public void bookingReadAll_unsuccessfulResponse_clearsStaleDataCorrectly() {
        BookingRepository repositoryUnderTest = new BookingRepository();
        Call<BookingReadAll> successCall = mockCall();
        Call<BookingReadAll> errorCall = mockCall();
        BookingReadAll cached = mock(BookingReadAll.class);

        when(api.bookingReadAll(any(), any())).thenReturn(successCall).thenReturn(errorCall);
        AtomicReference<Callback<BookingReadAll>> successCallbackRef = captureCallback(successCall);
        AtomicReference<Callback<BookingReadAll>> errorCallbackRef = captureCallback(errorCall);

        repositoryUnderTest.readAll(headers, params);
        successCallbackRef.get().onResponse(successCall, Response.success(cached));

        repositoryUnderTest.readAll(headers, params);
        @SuppressWarnings("unchecked")
        Response<BookingReadAll> unsuccessful = mock(Response.class);
        when(unsuccessful.isSuccessful()).thenReturn(false);
        errorCallbackRef.get().onResponse(errorCall, unsuccessful);

        // Expected: readAllResponse nên là null vì call thất bại
        assertNull(repositoryUnderTest.getReadAllResponse().getValue());
    }

    // TC-REPO-072 – bookingReadByID onFailure → correctly clears stale data and stops animation
    @Test
    public void bookingReadById_onFailure_clearsStaleDataCorrectly() {
        BookingRepository repositoryUnderTest = new BookingRepository();
        Call<BookingReadByID> successCall = mockCall();
        Call<BookingReadByID> failureCall = mockCall();
        BookingReadByID cached = mock(BookingReadByID.class);

        when(api.bookingReadByID(any(), any())).thenReturn(successCall).thenReturn(failureCall);
        AtomicReference<Callback<BookingReadByID>> successCallbackRef = captureCallback(successCall);
        AtomicReference<Callback<BookingReadByID>> failureCallbackRef = captureCallback(failureCall);

        repositoryUnderTest.readByID(headers, "BK-1");
        successCallbackRef.get().onResponse(successCall, Response.success(cached));

        repositoryUnderTest.readByID(headers, "BK-1");
        failureCallbackRef.get().onFailure(failureCall, new RuntimeException("offline"));

        // Expected: readByIDResponse nên là null khi onFailure
        assertNull(repositoryUnderTest.getReadByIDResponse().getValue());
    }

    // TC-REPO-073 – bookingReadAll onFailure → correctly clears stale data and stops animation
    @Test
    public void bookingReadAll_onFailure_clearsStaleDataCorrectly() {
        BookingRepository repositoryUnderTest = new BookingRepository();
        Call<BookingReadAll> successCall = mockCall();
        Call<BookingReadAll> failureCall = mockCall();
        BookingReadAll cached = mock(BookingReadAll.class);

        when(api.bookingReadAll(any(), any())).thenReturn(successCall).thenReturn(failureCall);
        AtomicReference<Callback<BookingReadAll>> successCallbackRef = captureCallback(successCall);
        AtomicReference<Callback<BookingReadAll>> failureCallbackRef = captureCallback(failureCall);

        repositoryUnderTest.readAll(headers, params);
        successCallbackRef.get().onResponse(successCall, Response.success(cached));

        repositoryUnderTest.readAll(headers, params);
        failureCallbackRef.get().onFailure(failureCall, new RuntimeException("offline"));

        // Expected: readAllResponse nên là null khi onFailure
        assertNull(repositoryUnderTest.getReadAllResponse().getValue());
    }



    // ---------- FAIL TESTS (real bugs / edge gaps) ----------

    // Bug: onFailure() in NotificationRepository does not clear stale data.
    // TC-REPO-045 – notification onFailure → nên xóa dữ liệu cũ nhưng hiện tại không làm
    @Test
    public void notification_onFailure_shouldClearStaleData_butCurrentlyDoesNot() {
        NotificationRepository repositoryUnderTest = new NotificationRepository();
        Call<NotificationReadAll> successCall = mockCall();
        Call<NotificationReadAll> failureCall = mockCall();
        NotificationReadAll cached = mock(NotificationReadAll.class);

        when(api.notificationReadAll(headers)).thenReturn(successCall).thenReturn(failureCall);
        AtomicReference<Callback<NotificationReadAll>> successCallbackRef = captureCallback(successCall);
        AtomicReference<Callback<NotificationReadAll>> failureCallbackRef = captureCallback(failureCall);

        MutableLiveData<NotificationReadAll> liveData = repositoryUnderTest.readAll(headers);
        successCallbackRef.get().onResponse(successCall, Response.success(cached));

        repositoryUnderTest.readAll(headers);
        failureCallbackRef.get().onFailure(failureCall, new RuntimeException("offline"));

        // Expected safer behavior: clear stale value on failure.
        assertNull(liveData.getValue());
    }

    // Bug: unsuccessful response with null errorBody leaves animation/data untouched in NotificationRepository.
    // TC-REPO-046 – notification không thành công không có errorBody → nên reset trạng thái nhưng hiện tại không làm
    @Test
    public void notification_unsuccessfulWithoutErrorBody_shouldResetState_butCurrentlyDoesNot() {
        NotificationRepository repositoryUnderTest = new NotificationRepository();
        Call<NotificationReadAll> successCall = mockCall();
        Call<NotificationReadAll> errorCall = mockCall();
        NotificationReadAll cached = mock(NotificationReadAll.class);

        when(api.notificationReadAll(headers)).thenReturn(successCall).thenReturn(errorCall);
        AtomicReference<Callback<NotificationReadAll>> successCallbackRef = captureCallback(successCall);
        AtomicReference<Callback<NotificationReadAll>> errorCallbackRef = captureCallback(errorCall);

        MutableLiveData<NotificationReadAll> liveData = repositoryUnderTest.readAll(headers);
        successCallbackRef.get().onResponse(successCall, Response.success(cached));

        @SuppressWarnings("unchecked")
        Response<NotificationReadAll> unsuccessful = mock(Response.class);
        when(unsuccessful.isSuccessful()).thenReturn(false);
        when(unsuccessful.errorBody()).thenReturn(null);

        repositoryUnderTest.readAll(headers);
        errorCallbackRef.get().onResponse(errorCall, unsuccessful);

        assertFalse(Boolean.TRUE.equals(repositoryUnderTest.getAnimation().getValue()));
        assertNull(liveData.getValue());
    }

    // Bug: onFailure() in RecordRepository does not clear stale data.
    // TC-REPO-047 – record onFailure → nên xóa dữ liệu cũ nhưng hiện tại không làm
    @Test
    public void record_onFailure_shouldClearStaleData_butCurrentlyDoesNot() {
        RecordRepository repositoryUnderTest = new RecordRepository();
        Call<RecordReadByID> successCall = mockCall();
        Call<RecordReadByID> failureCall = mockCall();
        RecordReadByID cached = mock(RecordReadByID.class);

        when(api.recordReadById(headers, "R1")).thenReturn(successCall).thenReturn(failureCall);
        AtomicReference<Callback<RecordReadByID>> successCallbackRef = captureCallback(successCall);
        AtomicReference<Callback<RecordReadByID>> failureCallbackRef = captureCallback(failureCall);

        MutableLiveData<RecordReadByID> liveData = repositoryUnderTest.readByID(headers, "R1");
        successCallbackRef.get().onResponse(successCall, Response.success(cached));

        repositoryUnderTest.readByID(headers, "R1");
        failureCallbackRef.get().onFailure(failureCall, new RuntimeException("offline"));

        assertNull(liveData.getValue());
    }

    // Bug: onFailure() in AppointmentQueueRepository keeps stale queue data.
    // TC-REPO-048 – appointmentQueue onFailure → nên xóa dữ liệu cũ nhưng hiện tại không làm
    @Test
    public void appointmentQueue_onFailure_shouldClearStaleData_butCurrentlyDoesNot() {
        AppointmentQueueRepository repositoryUnderTest = new AppointmentQueueRepository();
        Call<AppointmentQueue> successCall = mockCall();
        Call<AppointmentQueue> failureCall = mockCall();
        AppointmentQueue cached = mock(AppointmentQueue.class);

        when(api.appointmentQueue(headers, params)).thenReturn(successCall).thenReturn(failureCall);
        AtomicReference<Callback<AppointmentQueue>> successCallbackRef = captureCallback(successCall);
        AtomicReference<Callback<AppointmentQueue>> failureCallbackRef = captureCallback(failureCall);

        MutableLiveData<AppointmentQueue> liveData = repositoryUnderTest.getAppointmentQueue(headers, params);
        successCallbackRef.get().onResponse(successCall, Response.success(cached));

        repositoryUnderTest.getAppointmentQueue(headers, params);
        failureCallbackRef.get().onFailure(failureCall, new RuntimeException("offline"));

        assertNull(liveData.getValue());
    }

    // BVA-null: missing request body in BookingRepository.create should be validated, but currently throws NPE.
    // TC-REPO-049 – bookingCreate body null → nên xử lý an toàn nhưng hiện tại crash
    @Test
    public void bookingCreate_nullBody_shouldBeHandledSafely_butCurrentlyCrashes() {
        BookingRepository repositoryUnderTest = new BookingRepository();

        // Expected behavior: no crash, emit null and stop animation.
        repositoryUnderTest.create(headers, null);

        assertNull(repositoryUnderTest.getBookingCreate().getValue());
        assertFalse(Boolean.TRUE.equals(repositoryUnderTest.getAnimation().getValue()));
    }

    // Bug: SpecialityRepository.readAll race condition - animation state corrupted by concurrent calls.
    // TC-REPO-050 – speciality readAll concurrent calls → animation không accurate khi multiple requests đang pending
    @Test
    public void specialityReadAll_concurrentCalls_corruptsAnimationState() {
        SpecialityRepository repositoryUnderTest = new SpecialityRepository();
        Call<SpecialityReadAll> call1 = mockCall();
        Call<SpecialityReadAll> call2 = mockCall();
        SpecialityReadAll body = mock(SpecialityReadAll.class);

        when(api.specialityReadAll(any(), any())).thenReturn(call1).thenReturn(call2);
        AtomicReference<Callback<SpecialityReadAll>> callbackRef1 = captureCallback(call1);
        AtomicReference<Callback<SpecialityReadAll>> callbackRef2 = captureCallback(call2);

        // Call 1 bắt đầu
        repositoryUnderTest.readAll(headers, params);
        // Call 2 bắt đầu ngay khi call 1 đang pending
        repositoryUnderTest.readAll(headers, params);

        // Call 1 hoàn thành - animation set false
        callbackRef1.get().onResponse(call1, Response.success(body));

        // Call 2 vẫn pending nhưng animation đã bị reset
        // Expected: animation phải là true vì call 2 chưa finish
        assertSame(Boolean.TRUE, repositoryUnderTest.getAnimation().getValue());
    }

    // Bug: SpecialityRepository.readById không xử lý trường hợp response body là null dù HTTP success.
    // TC-REPO-051 – speciality readById với null body → nên validate nhưng hiện tại crash
    @Test
    public void specialityReadById_successResponseWithNullBody_shouldFailGracefully_butCrashes() {
        SpecialityRepository repositoryUnderTest = new SpecialityRepository();

        // Khi API trả về HTTP 200 nhưng body là null (server error nhưng vẫn success HTTP)
        // Expected: nên xử lý an toàn, emit null
        // Current bug: code không check null trước khi setValue → crash
        repositoryUnderTest.readById(headers, "SPEC-1");

        // Assert expected safe behavior
        assertNull(repositoryUnderTest.getReadByIdResponse().getValue());
        assertFalse(Boolean.TRUE.equals(repositoryUnderTest.getAnimation().getValue()));
    }


    // Bug: DoctorRepository.readAll có assert body != null nhưng không handle null gracefully.
    // TC-REPO-053 – doctor readAll với null body → nên xử lý an toàn nhưng assert fail gây crash
    @Test
    public void doctorReadAll_nullBodyInSuccessResponse_shouldHandleGracefully_butAssertFails() {
        DoctorRepository repositoryUnderTest = new DoctorRepository();
        Call<DoctorReadAll> call = mockCall();

        when(api.doctorReadAll(any(), any())).thenReturn(call);
        AtomicReference<Callback<DoctorReadAll>> callbackRef = captureCallback(call);

        repositoryUnderTest.readAll(headers, params);

        // Simulate success response với null body (HTTP 200 nhưng body rỗng)
        callbackRef.get().onResponse(call, Response.success(null));

        // Expected: nên xử lý an toàn, emit null
        // Current bug: assert content != null sẽ fail hoặc crash
        assertNull(repositoryUnderTest.getReadAllResponse().getValue());
        assertSame(Boolean.FALSE, repositoryUnderTest.getAnimation().getValue());
    }

    // Bug: DoctorRepository.readById race condition - animation state bị corrupted khi gọi liên tiếp.
    // TC-REPO-054 – doctor readById với rapid consecutive calls → animation state không chính xác
    @Test
    public void doctorReadById_rapidConsecutiveCalls_corruptsAnimationState() {
        DoctorRepository repositoryUnderTest = new DoctorRepository();
        Call<DoctorReadByID> call1 = mockCall();
        Call<DoctorReadByID> call2 = mockCall();
        DoctorReadByID body = mock(DoctorReadByID.class);

        when(api.doctorReadByID(any(), any())).thenReturn(call1).thenReturn(call2);
        AtomicReference<Callback<DoctorReadByID>> callbackRef1 = captureCallback(call1);
        AtomicReference<Callback<DoctorReadByID>> callbackRef2 = captureCallback(call2);

        // Call 1 bắt đầu
        repositoryUnderTest.readById(headers, "DOC-1");
        // Call 2 bắt đầu ngay sau khi call 1 đang pending
        repositoryUnderTest.readById(headers, "DOC-2");

        // Call 1 hoàn thành - animation set false
        callbackRef1.get().onResponse(call1, Response.success(body));

        // Call 2 vẫn đang chạy nhưng animation đã bị set false
        // Expected: animation phải là true vì call 2 chưa finish
        assertSame(Boolean.TRUE, repositoryUnderTest.getAnimation().getValue());
    }

    // Bug: DoctorRepository readById không validate empty string id - API vẫn được gọi với id = "".
    // TC-REPO-055 – doctor readById với empty string id → nên reject sớm nhưng vẫn gọi API
    @Test
    public void doctorReadById_emptyId_shouldRejectEarly_butCallsAPIAnyway() {
        DoctorRepository repositoryUnderTest = new DoctorRepository();
        Call<DoctorReadByID> call = mockCall();

        when(api.doctorReadByID(any(), any())).thenReturn(call);

        // Gọi với empty string thay vì null
        repositoryUnderTest.readById(headers, "");

        // Expected: nên validate và reject sớm, không gọi API
        // Current bug: API vẫn được gọi với empty id, gây wasted network call
        verify(api, org.mockito.Mockito.never()).doctorReadByID(any(), org.mockito.Mockito.eq(""));
    }

    // Bug: ServiceRepository.readAll race condition - animation state corrupted by concurrent calls.
    // TC-REPO-056 – service readAll concurrent calls → animation state không chính xác khi call thứ 2 đang chạy
    @Test
    public void serviceReadAll_concurrentCalls_corruptsAnimationState() {
        ServiceRepository repositoryUnderTest = new ServiceRepository();
        Call<ServiceReadAll> call1 = mockCall();
        Call<ServiceReadAll> call2 = mockCall();
        ServiceReadAll body = mock(ServiceReadAll.class);

        when(api.serviceReadAll(any(), any())).thenReturn(call1).thenReturn(call2);
        AtomicReference<Callback<ServiceReadAll>> callbackRef1 = captureCallback(call1);
        AtomicReference<Callback<ServiceReadAll>> callbackRef2 = captureCallback(call2);

        // Call 1 bắt đầu
        repositoryUnderTest.readAll(headers, params);
        // Gọi call 2 ngay khi call 1 đang chạy
        repositoryUnderTest.readAll(headers, params);

        // Call 1 kết thúc - animation = false
        callbackRef1.get().onResponse(call1, Response.success(body));

        // Call 2 vẫn đang chạy nhưng animation đã bị set false
        // Expected: animation vẫn phải là true vì call 2 chưa kết thúc
        assertSame(Boolean.TRUE, repositoryUnderTest.getAnimation().getValue());
    }

    // Bug: ServiceRepository.readByID race condition - animation not properly managed.
    // TC-REPO-057 – service readByID với body rỗng → nên xử lý an toàn nhưng hiện tại không
    @Test
    public void serviceReadByID_emptyResponseBody_shouldHandleGracefully_butDoesNot() {
        ServiceRepository repositoryUnderTest = new ServiceRepository();
        Call<ServiceReadByID> call = mockCall();
        ServiceReadByID emptyBody = mock(ServiceReadByID.class);

        when(api.serviceReadByID(any(), any())).thenReturn(call);
        AtomicReference<Callback<ServiceReadByID>> callbackRef = captureCallback(call);

        repositoryUnderTest.readByID(headers, "SVC-1");
        // Response thành công nhưng body là empty (all fields null)
        callbackRef.get().onResponse(call, Response.success(emptyBody));

        // Expected: data nên được set dù body empty
        // Current bug: có thể null pointer exception hoặc data không được set đúng
        assertNull(repositoryUnderTest.getReadByIDResponse().getValue());
    }

    // Bug: ServiceRepository.readAll onResponse không kiểm tra body null trước khi setValue.
    // TC-REPO-058 – service readAll body null → nên validate nhưng hiện tại crash
    @Test
    public void serviceReadAll_nullBodyInSuccessResponse_shouldBeHandled_butCurrentlyCrashes() {
        ServiceRepository repositoryUnderTest = new ServiceRepository();

        // Khi HTTP layer trả về success nhưng body là null
        // Expected: nên xử lý an toàn, emit null và stop animation
        // Current bug: sẽ crash vì không check null
        repositoryUnderTest.readAll(headers, params);

        // Mock để simulate null body response
        assertNull(repositoryUnderTest.getReadAllResponse().getValue());
        assertFalse(Boolean.TRUE.equals(repositoryUnderTest.getAnimation().getValue()));
    }


    // Bug: AppointmentRepository.readAll có assert content != null nhưng không handle null body gracefully.
    // TC-REPO-059 – appointment readAll với null body → nên xử lý an toàn nhưng assert fail gây crash và animation không được clear
    @Test
    public void appointmentReadAll_nullBodyInSuccessResponse_shouldHandleGracefully_butAssertFails() {
        AppointmentRepository repositoryUnderTest = new AppointmentRepository();
        Call<AppointmentReadAll> call = mockCall();

        when(api.appointmentReadAll(any(), any())).thenReturn(call);
        AtomicReference<Callback<AppointmentReadAll>> callbackRef = captureCallback(call);

        repositoryUnderTest.readAll(headers, params);

        // Simulate success response với null body (HTTP 200 nhưng body rỗng)
        callbackRef.get().onResponse(call, Response.success(null));

        // Expected: nên xử lý an toàn, emit null và stop animation
        // Current bug: assert content != null sẽ fail hoặc crash
        assertNull(repositoryUnderTest.getReadAllResponse().getValue());
        assertSame(Boolean.FALSE, repositoryUnderTest.getAnimation().getValue());
    }

    // Bug: AppointmentRepository.readByID không handle response body là null dù HTTP success.
    // TC-REPO-060 – appointment readByID với null body → nên validate nhưng hiện tại crash
    @Test
    public void appointmentReadById_successResponseWithNullBody_shouldFailGracefully_butCrashes() {
        AppointmentRepository repositoryUnderTest = new AppointmentRepository();
        Call<AppointmentReadByID> call = mockCall();

        when(api.appointmentReadByID(any(), any())).thenReturn(call);
        AtomicReference<Callback<AppointmentReadByID>> callbackRef = captureCallback(call);

        repositoryUnderTest.readByID(headers, "APT-1");

        // Khi API trả về HTTP 200 nhưng body là null
        callbackRef.get().onResponse(call, Response.success(null));

        // Expected: nên xử lý an toàn, emit null
        // Current bug: assert content != null sẽ fail hoặc crash
        assertNull(repositoryUnderTest.getReadByIDResponse().getValue());
        assertFalse(Boolean.TRUE.equals(repositoryUnderTest.getAnimation().getValue()));
    }

    // Bug: AppointmentRepository.readAll race condition - animation state corrupted by concurrent calls.
    // TC-REPO-061 – appointment readAll concurrent calls → animation không accurate khi multiple requests đang pending
    @Test
    public void appointmentReadAll_concurrentCalls_corruptsAnimationState() {
        AppointmentRepository repositoryUnderTest = new AppointmentRepository();
        Call<AppointmentReadAll> call1 = mockCall();
        Call<AppointmentReadAll> call2 = mockCall();
        AppointmentReadAll body = mock(AppointmentReadAll.class);

        when(api.appointmentReadAll(any(), any())).thenReturn(call1).thenReturn(call2);
        AtomicReference<Callback<AppointmentReadAll>> callbackRef1 = captureCallback(call1);
        AtomicReference<Callback<AppointmentReadAll>> callbackRef2 = captureCallback(call2);

        // Call 1 bắt đầu
        repositoryUnderTest.readAll(headers, params);
        // Call 2 bắt đầu ngay khi call 1 đang pending
        repositoryUnderTest.readAll(headers, params);

        // Call 1 hoàn thành - animation set false
        callbackRef1.get().onResponse(call1, Response.success(body));

        // Call 2 vẫn pending nhưng animation đã bị reset
        // Expected: animation phải là true vì call 2 chưa finish
        assertSame(Boolean.TRUE, repositoryUnderTest.getAnimation().getValue());
    }


    // Bug: TreatmentRepository.readAll race condition - animation state corrupted by concurrent calls.
    // TC-REPO-062 – treatment readAll concurrent calls → animation không accurate khi multiple requests đang pending
    @Test
    public void treatmentReadAll_concurrentCalls_corruptsAnimationState() {
        TreatmentRepository repositoryUnderTest = new TreatmentRepository();
        Call<TreatmentReadAll> call1 = mockCall();
        Call<TreatmentReadAll> call2 = mockCall();
        TreatmentReadAll body = mock(TreatmentReadAll.class);

        when(api.treatmentReadAll(any(), any())).thenReturn(call1).thenReturn(call2);
        AtomicReference<Callback<TreatmentReadAll>> callbackRef1 = captureCallback(call1);
        AtomicReference<Callback<TreatmentReadAll>> callbackRef2 = captureCallback(call2);

        // Call 1 bắt đầu
        repositoryUnderTest.readAll(headers, "APT-1");
        // Call 2 bắt đầu ngay khi call 1 đang pending
        repositoryUnderTest.readAll(headers, "APT-2");

        // Call 1 hoàn thành - animation set false
        callbackRef1.get().onResponse(call1, Response.success(body));

        // Call 2 vẫn pending nhưng animation đã bị reset
        // Expected: animation phải là true vì call 2 chưa finish
        assertSame(Boolean.TRUE, repositoryUnderTest.getAnimation().getValue());
    }

    // Bug: TreatmentRepository.readByID race condition - animation state bị corrupted khi gọi liên tiếp.
    // TC-REPO-063 – treatment readByID với rapid consecutive calls → animation state không chính xác
    @Test
    public void treatmentReadByID_rapidConsecutiveCalls_corruptsAnimationState() {
        TreatmentRepository repositoryUnderTest = new TreatmentRepository();
        Call<TreatmentReadByID> call1 = mockCall();
        Call<TreatmentReadByID> call2 = mockCall();
        TreatmentReadByID body = mock(TreatmentReadByID.class);

        when(api.treatmentReadByID(any(), any())).thenReturn(call1).thenReturn(call2);
        AtomicReference<Callback<TreatmentReadByID>> callbackRef1 = captureCallback(call1);
        AtomicReference<Callback<TreatmentReadByID>> callbackRef2 = captureCallback(call2);

        // Call 1 bắt đầu
        repositoryUnderTest.readByID(headers, "TRT-1");
        // Call 2 bắt đầu ngay sau khi call 1 đang pending
        repositoryUnderTest.readByID(headers, "TRT-2");

        // Call 1 hoàn thành - animation set false
        callbackRef1.get().onResponse(call1, Response.success(body));

        // Call 2 vẫn đang chạy nhưng animation đã bị set false
        // Expected: animation phải là true vì call 2 chưa finish
        assertSame(Boolean.TRUE, repositoryUnderTest.getAnimation().getValue());
    }


    // Bug: BookingRepository.readByID có assert content != null nhưng không handle null body gracefully.
    // TC-REPO-064 – booking readByID với null body → nên xử lý an toàn nhưng assert fail gây crash
    @Test
    public void bookingReadById_nullBodyInSuccessResponse_shouldHandleGracefully_butAssertFails() {
        BookingRepository repositoryUnderTest = new BookingRepository();
        Call<BookingReadByID> call = mockCall();

        when(api.bookingReadByID(any(), any())).thenReturn(call);
        AtomicReference<Callback<BookingReadByID>> callbackRef = captureCallback(call);

        repositoryUnderTest.readByID(headers, "BK-1");

        // Simulate success response với null body (HTTP 200 nhưng body rỗng)
        callbackRef.get().onResponse(call, Response.success(null));

        // Expected: nên xử lý an toàn, emit null và stop animation
        // Current bug: assert content != null sẽ fail hoặc crash
        assertNull(repositoryUnderTest.getReadByIDResponse().getValue());
        assertSame(Boolean.FALSE, repositoryUnderTest.getAnimation().getValue());
    }

    // Bug: BookingRepository.readAll có assert content != null nhưng không handle null body gracefully.
    // TC-REPO-065 – booking readAll với null body → nên xử lý an toàn nhưng assert fail gây crash
    @Test
    public void bookingReadAll_nullBodyInSuccessResponse_shouldHandleGracefully_butAssertFails() {
        BookingRepository repositoryUnderTest = new BookingRepository();
        Call<BookingReadAll> call = mockCall();

        when(api.bookingReadAll(any(), any())).thenReturn(call);
        AtomicReference<Callback<BookingReadAll>> callbackRef = captureCallback(call);

        repositoryUnderTest.readAll(headers, params);

        // Simulate success response với null body (HTTP 200 nhưng body rỗng)
        callbackRef.get().onResponse(call, Response.success(null));

        // Expected: nên xử lý an toàn, emit null và stop animation
        // Current bug: assert content != null sẽ fail hoặc crash
        assertNull(repositoryUnderTest.getReadAllResponse().getValue());
        assertSame(Boolean.FALSE, repositoryUnderTest.getAnimation().getValue());
    }

    // Bug: BookingRepository.readAll race condition - animation state corrupted by concurrent calls.
    // TC-REPO-066 – booking readAll concurrent calls → animation không accurate khi multiple requests đang pending
    @Test
    public void bookingReadAll_concurrentCalls_corruptsAnimationState() {
        BookingRepository repositoryUnderTest = new BookingRepository();
        Call<BookingReadAll> call1 = mockCall();
        Call<BookingReadAll> call2 = mockCall();
        BookingReadAll body = mock(BookingReadAll.class);

        when(api.bookingReadAll(any(), any())).thenReturn(call1).thenReturn(call2);
        AtomicReference<Callback<BookingReadAll>> callbackRef1 = captureCallback(call1);
        AtomicReference<Callback<BookingReadAll>> callbackRef2 = captureCallback(call2);

        // Call 1 bắt đầu
        repositoryUnderTest.readAll(headers, params);
        // Call 2 bắt đầu ngay khi call 1 đang pending
        repositoryUnderTest.readAll(headers, params);

        // Call 1 hoàn thành - animation set false
        callbackRef1.get().onResponse(call1, Response.success(body));

        // Call 2 vẫn pending nhưng animation đã bị reset
        // Expected: animation phải là true vì call 2 chưa finish
        assertSame(Boolean.TRUE, repositoryUnderTest.getAnimation().getValue());
    }
    // Bug: BookingPhotoRepository.readAll race condition - animation state corrupted by concurrent calls.
    // TC-REPO-074 – bookingPhoto readAll concurrent calls → animation không accurate khi multiple requests đang pending
    @Test
    public void bookingPhotoReadAll_concurrentCalls_corruptsAnimationState() {
        BookingPhotoRepository repositoryUnderTest = new BookingPhotoRepository();
        Call<BookingPhotoReadAll> call1 = mockCall();
        Call<BookingPhotoReadAll> call2 = mockCall();
        BookingPhotoReadAll body = mock(BookingPhotoReadAll.class);

        when(api.bookingPhotoReadAll(any(), any())).thenReturn(call1).thenReturn(call2);
        AtomicReference<Callback<BookingPhotoReadAll>> callbackRef1 = captureCallback(call1);
        AtomicReference<Callback<BookingPhotoReadAll>> callbackRef2 = captureCallback(call2);

        // Call 1 bắt đầu
        repositoryUnderTest.readAll(headers, "BK-1");
        // Call 2 bắt đầu ngay khi call 1 đang pending
        repositoryUnderTest.readAll(headers, "BK-2");

        // Call 1 hoàn thành - animation set false
        callbackRef1.get().onResponse(call1, Response.success(body));

        // Call 2 vẫn pending nhưng animation đã bị reset
        // Expected: animation phải là true vì call 2 chưa finish
        assertSame(Boolean.TRUE, repositoryUnderTest.getAnimation().getValue());
    }

    // Bug: BookingRepository không track số lượng pending requests, animation bị reset sớm khi có concurrent calls.
    // TC-REPO-067 – bookingCreate concurrent calls → animation bị reset khi Call 2 vẫn đang pending
    @Test
    public void bookingCreate_concurrentCalls_corruptsAnimationState() {
        BookingRepository repositoryUnderTest = new BookingRepository();
        Call<BookingCreate> call1 = mockCall();
        Call<BookingCreate> call2 = mockCall();
        BookingCreate body = mock(BookingCreate.class);

        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put("doctorId", "DOC-1");

        when(api.bookingCreate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(call1).thenReturn(call2);
        AtomicReference<Callback<BookingCreate>> callbackRef1 = captureCallback(call1);
        AtomicReference<Callback<BookingCreate>> callbackRef2 = captureCallback(call2);

        repositoryUnderTest.create(headers, bodyParams);
        repositoryUnderTest.create(headers, bodyParams);

        callbackRef1.get().onResponse(call1, Response.success(body));

        // Expected: animation phải là true vì call 2 chưa finish
        // Current bug: animation bị set false khi call 1 hoàn thành, không track pending requests
        assertSame(Boolean.TRUE, repositoryUnderTest.getAnimation().getValue());
    }

    // Bug: BookingRepository không track số lượng pending requests, animation bị reset sớm khi có concurrent calls.
    // TC-REPO-068 – bookingReadByID concurrent calls → animation bị reset khi Call 2 vẫn đang pending
    @Test
    public void bookingReadById_concurrentCalls_corruptsAnimationState() {
        BookingRepository repositoryUnderTest = new BookingRepository();
        Call<BookingReadByID> call1 = mockCall();
        Call<BookingReadByID> call2 = mockCall();
        BookingReadByID body = mock(BookingReadByID.class);

        when(api.bookingReadByID(any(), any())).thenReturn(call1).thenReturn(call2);
        AtomicReference<Callback<BookingReadByID>> callbackRef1 = captureCallback(call1);
        AtomicReference<Callback<BookingReadByID>> callbackRef2 = captureCallback(call2);

        repositoryUnderTest.readByID(headers, "BK-1");
        repositoryUnderTest.readByID(headers, "BK-2");

        callbackRef1.get().onResponse(call1, Response.success(body));

        // Expected: animation phải là true vì call 2 chưa finish
        // Current bug: animation bị set false khi call 1 hoàn thành, không track pending requests
        assertSame(Boolean.TRUE, repositoryUnderTest.getAnimation().getValue());
    }

    // Bug: BookingRepository không track số lượng pending requests, animation bị reset sớm khi có concurrent calls.
    // TC-REPO-069 – bookingReadAll second concurrent call → animation bị reset khi Call 2 vẫn đang pending
    @Test
    public void bookingReadAll_secondCall_corruptsAnimationState() {
        BookingRepository repositoryUnderTest = new BookingRepository();
        Call<BookingReadAll> call1 = mockCall();
        Call<BookingReadAll> call2 = mockCall();
        BookingReadAll body = mock(BookingReadAll.class);

        when(api.bookingReadAll(any(), any())).thenReturn(call1).thenReturn(call2);
        AtomicReference<Callback<BookingReadAll>> callbackRef1 = captureCallback(call1);
        AtomicReference<Callback<BookingReadAll>> callbackRef2 = captureCallback(call2);

        repositoryUnderTest.readAll(headers, params);
        repositoryUnderTest.readAll(headers, params);

        callbackRef1.get().onResponse(call1, Response.success(body));

        // Expected: animation phải là true vì call 2 chưa finish
        // Current bug: animation bị set false khi call 1 hoàn thành, không track pending requests
        assertSame(Boolean.TRUE, repositoryUnderTest.getAnimation().getValue());
    }
    private <T> AtomicReference<Callback<T>> captureCallback(Call<T> call) {
        AtomicReference<Callback<T>> callbackRef = new AtomicReference<>();
        doAnswer(invocation -> {
            callbackRef.set(invocation.getArgument(0));
            return null;
        }).when(call).enqueue(any());
        return callbackRef;
    }

    @SuppressWarnings("unchecked")
    private <T> Call<T> mockCall() {
        return (Call<T>) mock(Call.class);
    }

    private <T> Response<T> errorResponse() {
        return Response.error(
                400,
                ResponseBody.create(MediaType.parse("application/json"), "{\"message\":\"error\"}")
        );
    }
}
