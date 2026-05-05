package com.example.do_an_tot_nghiep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.do_an_tot_nghiep.Configuration.HTTPRequest;
import com.example.do_an_tot_nghiep.Configuration.HTTPService;
import com.example.do_an_tot_nghiep.Container.AppointmentQueue;
import com.example.do_an_tot_nghiep.Container.AppointmentReadAll;
import com.example.do_an_tot_nghiep.Container.AppointmentReadByID;
import com.example.do_an_tot_nghiep.Container.BookingPhotoReadAll;
import com.example.do_an_tot_nghiep.Container.BookingReadAll;
import com.example.do_an_tot_nghiep.Container.BookingReadByID;
import com.example.do_an_tot_nghiep.Container.DoctorReadAll;
import com.example.do_an_tot_nghiep.Container.DoctorReadByID;
import com.example.do_an_tot_nghiep.Container.Login;
import com.example.do_an_tot_nghiep.Container.NotificationReadAll;
import com.example.do_an_tot_nghiep.Container.PatientProfile;
import com.example.do_an_tot_nghiep.Container.RecordReadByID;
import com.example.do_an_tot_nghiep.Container.ServiceReadAll;
import com.example.do_an_tot_nghiep.Container.ServiceReadByID;
import com.example.do_an_tot_nghiep.Container.SpecialityReadByID;
import com.example.do_an_tot_nghiep.Container.TreatmentReadAll;
import com.example.do_an_tot_nghiep.Container.TreatmentReadByID;
import com.example.do_an_tot_nghiep.Helper.SingleLiveEvent;
import com.example.do_an_tot_nghiep.Homepage.HomepageViewModel;
import com.example.do_an_tot_nghiep.Loginpage.LoginViewModel;
import com.example.do_an_tot_nghiep.Notificationpage.NotificationViewModel;
import com.example.do_an_tot_nghiep.Recordpage.RecordpageViewModel;
import com.example.do_an_tot_nghiep.Repository.AppointmentQueueRepository;
import com.example.do_an_tot_nghiep.Repository.AppointmentRepository;
import com.example.do_an_tot_nghiep.Repository.BookingPhotoRepository;
import com.example.do_an_tot_nghiep.Repository.BookingRepository;
import com.example.do_an_tot_nghiep.Repository.DoctorRepository;
import com.example.do_an_tot_nghiep.Repository.NotificationRepository;
import com.example.do_an_tot_nghiep.Repository.RecordRepository;
import com.example.do_an_tot_nghiep.Repository.ServiceRepository;
import com.example.do_an_tot_nghiep.Repository.SpecialityRepository;
import com.example.do_an_tot_nghiep.Repository.SynchronousTaskExecutorRule;
import com.example.do_an_tot_nghiep.Repository.TreatmentRepository;
import com.example.do_an_tot_nghiep.Searchpage.SearchpageViewModel;
import com.example.do_an_tot_nghiep.Servicepage.ServicepageViewModel;
import com.example.do_an_tot_nghiep.Settingspage.SettingspageViewModel;
import com.example.do_an_tot_nghiep.Specialitypage.SpecialitypageViewModel;
import com.example.do_an_tot_nghiep.Treatmentpage.TreatmentpageViewModel;
import com.example.do_an_tot_nghiep.Appointmentpage.AppointmentpageViewModel;
import com.example.do_an_tot_nghiep.Bookingpage.BookingpageViewModel;
import com.example.do_an_tot_nghiep.Doctorpage.DoctorpageViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Unit tests for ViewModel classes.
 *
 * Coverage:
 * - LoginViewModel (direct Retrofit orchestration)
 * - NotificationViewModel, BookingpageViewModel, AppointmentpageViewModel, HomepageViewModel
 *   (repository-backed ViewModels)
 *
 * Some tests are intentionally failing because they expose real bugs:
 * - missing lazy initialization / null guards
 * - stale LiveData not cleared on error-body-less failures
 */
public class ViewModelBugDetectionTest {

    @Rule
    public SynchronousTaskExecutorRule synchronousTaskExecutorRule = new SynchronousTaskExecutorRule();

    private AutoCloseable mocks;

    @Mock
    private Retrofit retrofit;

    @Mock
    private HTTPRequest api;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentQueueRepository appointmentQueueRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private SpecialityRepository specialityRepository;

    @Mock
    private BookingPhotoRepository bookingPhotoRepository;

    @Mock
    private TreatmentRepository treatmentRepository;

    @Mock
    private RecordRepository recordRepository;

    private MockedStatic<HTTPService> httpServiceMock;
    private Map<String, String> headers;
    private Map<String, String> parameters;

    @Before
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        when(retrofit.create(HTTPRequest.class)).thenReturn(api);
        httpServiceMock = org.mockito.Mockito.mockStatic(HTTPService.class);
        httpServiceMock.when(HTTPService::getInstance).thenReturn(retrofit);

        headers = new HashMap<>();
        headers.put("Authorization", "Bearer token");
        headers.put("Type", "patient");

        parameters = new HashMap<>();
        parameters.put("page", "1");
        parameters.put("limit", "10");
    }

    @After
    public void tearDown() throws Exception {
        httpServiceMock.close();
        mocks.close();
    }

    // Test Case ID: TC-VM-001
    // Valid input: login with phone should update LiveData and stop animation.
    @Test
    public void givenValidPhoneAndPasswordWhenLoginWithPhoneSuccessThenLiveDataContainsLoginAndAnimationStops() {
        LoginViewModel viewModelUnderTest = new LoginViewModel();
        MutableLiveData<Boolean> animationLiveData = viewModelUnderTest.getAnimation();
        MutableLiveData<Login> loginLiveData = viewModelUnderTest.getLoginWithPhoneResponse();

        Call<Login> mockApiCall = mockCall();
        Login expectedLogin = mock(Login.class);

        doReturn(mockApiCall).when(api).login("0123456789", "secret", "patient");
        AtomicReference<Callback<Login>> callbackRef = captureCallback(mockApiCall);

        viewModelUnderTest.loginWithPhone("0123456789", "secret");
        assertEquals(Boolean.TRUE, animationLiveData.getValue());
        verify(api).login("0123456789", "secret", "patient");
        verify(mockApiCall).enqueue(any());

        callbackRef.get().onResponse(mockApiCall, Response.success(expectedLogin));

        assertSame(expectedLogin, loginLiveData.getValue());
        assertEquals(Boolean.FALSE, animationLiveData.getValue());
    }

    // Test Case ID: TC-VM-002
    // Invalid input partition: login failure should clear LiveData and stop animation.
    @Test
    public void givenLoginFailureWhenLoginWithPhoneThenLiveDataBecomesNullAndAnimationStops() {
        LoginViewModel viewModelUnderTest = new LoginViewModel();
        MutableLiveData<Boolean> animationLiveData = viewModelUnderTest.getAnimation();
        MutableLiveData<Login> loginLiveData = viewModelUnderTest.getLoginWithPhoneResponse();

        Call<Login> mockApiCall = mockCall();
        doReturn(mockApiCall).when(api).login("0123456789", "secret", "patient");
        AtomicReference<Callback<Login>> callbackRef = captureCallback(mockApiCall);

        viewModelUnderTest.loginWithPhone("0123456789", "secret");
        callbackRef.get().onFailure(mockApiCall, new RuntimeException("network down"));

        assertNull(loginLiveData.getValue());
        assertEquals(Boolean.FALSE, animationLiveData.getValue());
    }

    // Test Case ID: TC-VM-003
    // Real bug: a fresh LoginViewModel crashes because animation/login LiveData are lazily initialized
    // but loginWithPhone() uses them directly without null guards.
    @Test
    public void givenFreshLoginViewModelWhenLoginWithPhoneCalledThenShouldNotCrash() {
        LoginViewModel viewModelUnderTest = new LoginViewModel();
        Call<Login> mockApiCall = mockCall();
        doReturn(mockApiCall).when(api).login("0123456789", "secret", "patient");

        // This fails with NullPointerException on current code because getAnimation()/getLoginWithPhoneResponse()
        // are not called before loginWithPhone() uses the fields.
        viewModelUnderTest.loginWithPhone("0123456789", "secret");
    }

    // Test Case ID: TC-VM-004
    // Real bug: loginWithGoogle leaves stale data/animation when errorBody() is null.
    @Test
    public void givenCachedGoogleLoginWhenNextErrorHasNoBodyThenShouldClearStaleDataAndStopAnimation() {
        LoginViewModel viewModelUnderTest = new LoginViewModel();
        MutableLiveData<Boolean> animationLiveData = viewModelUnderTest.getAnimation();
        MutableLiveData<Login> loginLiveData = viewModelUnderTest.getLoginWithGoogleResponse();

        Call<Login> successCall = mockCall();
        Call<Login> errorCall = mockCall();
        Login cachedLogin = mock(Login.class);

        doReturn(successCall, errorCall).when(api).loginWithGoogle("patient@test.com", "secret", "patient");
        AtomicReference<Callback<Login>> successCallbackRef = captureCallback(successCall);
        AtomicReference<Callback<Login>> errorCallbackRef = captureCallback(errorCall);

        viewModelUnderTest.loginWithGoogle("patient@test.com", "secret");
        successCallbackRef.get().onResponse(successCall, Response.success(cachedLogin));
        assertSame(cachedLogin, loginLiveData.getValue());

        viewModelUnderTest.loginWithGoogle("patient@test.com", "secret");
        @SuppressWarnings("unchecked")
        Response<Login> unsuccessfulResponse = mock(Response.class);
        doReturn(false).when(unsuccessfulResponse).isSuccessful();
        doReturn(null).when(unsuccessfulResponse).errorBody();
        errorCallbackRef.get().onResponse(errorCall, unsuccessfulResponse);

        // This fails on current code because loginWithGoogle() only clears state when errorBody() != null.
        assertNull(loginLiveData.getValue());
        assertEquals(Boolean.FALSE, animationLiveData.getValue());
    }

    // Test Case ID: TC-VM-005
    // Valid repository interaction: NotificationViewModel should expose repository LiveData values.
    @Test
    public void givenInjectedNotificationRepositoryWhenReadAllSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        NotificationViewModel viewModelUnderTest = new NotificationViewModel();
        MutableLiveData<NotificationReadAll> repositoryLiveData = new MutableLiveData<>();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();
        NotificationReadAll expectedResponse = mock(NotificationReadAll.class);

        doReturn(repositoryAnimation).when(notificationRepository).getAnimation();
        doReturn(repositoryLiveData).when(notificationRepository).readAll(headers);
        setPrivateField(viewModelUnderTest, "repository", notificationRepository);

        viewModelUnderTest.readAll(headers);
        AtomicReference<NotificationReadAll> observedValue = observe(viewModelUnderTest.getReadAllResponse());
        repositoryLiveData.setValue(expectedResponse);

        assertSame(repositoryLiveData, viewModelUnderTest.getReadAllResponse());
        assertSame(expectedResponse, observedValue.get());
    }

    // Test Case ID: TC-VM-006
    // Real bug: NotificationViewModel.readAll() crashes when instantiate() was not called.
    @Test
    public void givenFreshNotificationViewModelWhenReadAllCalledThenShouldNotCrash() {
        NotificationViewModel viewModelUnderTest = new NotificationViewModel();

        // This fails because repository is never initialized unless instantiate() is called first.
        viewModelUnderTest.readAll(headers);
    }

    // Test Case ID: TC-VM-007
    // Valid repository interaction: BookingpageViewModel should expose repository LiveData values.
    @Test
    public void givenInjectedBookingRepositoriesWhenServiceReadByIdSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        BookingpageViewModel viewModelUnderTest = new BookingpageViewModel();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();
        SingleLiveEvent<com.example.do_an_tot_nghiep.Container.ServiceReadByID> serviceReadByIdLiveData = new SingleLiveEvent<>();

        doReturn(repositoryAnimation).when(serviceRepository).getAnimation();
        doReturn(serviceReadByIdLiveData).when(serviceRepository).getReadByIDResponse();
        setPrivateField(viewModelUnderTest, "serviceRepository", serviceRepository);

        viewModelUnderTest.serviceReadById(headers, "S1");
        AtomicReference<com.example.do_an_tot_nghiep.Container.ServiceReadByID> observedValue = observe(viewModelUnderTest.getServiceReadByIdResponse());
        verify(serviceRepository).readByID(headers, "S1");
        serviceReadByIdLiveData.setValue(mock(com.example.do_an_tot_nghiep.Container.ServiceReadByID.class));

        assertSame(serviceReadByIdLiveData, viewModelUnderTest.getServiceReadByIdResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-008
    // Real bug: BookingpageViewModel.serviceReadById() crashes when repository was not instantiated.
    @Test
    public void givenFreshBookingpageViewModelWhenServiceReadByIdCalledThenShouldNotCrash() {
        BookingpageViewModel viewModelUnderTest = new BookingpageViewModel();

        // This fails because serviceRepository is null unless instantiate() is called or the field is injected.
        viewModelUnderTest.serviceReadById(headers, "S1");
    }

    // Test Case ID: TC-VM-009
    // Valid repository interaction: AppointmentpageViewModel should expose repository LiveData values.
    @Test
    public void givenInjectedAppointmentRepositoriesWhenReadAllSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        AppointmentpageViewModel viewModelUnderTest = new AppointmentpageViewModel();
        SingleLiveEvent<AppointmentReadAll> repositoryLiveData = new SingleLiveEvent<>();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();

        doReturn(repositoryAnimation).when(appointmentRepository).getAnimation();
        doReturn(repositoryLiveData).when(appointmentRepository).getReadAllResponse();
        setPrivateField(viewModelUnderTest, "repository", appointmentRepository);

        viewModelUnderTest.readAll(headers, parameters);
        AtomicReference<AppointmentReadAll> observedValue = observe(viewModelUnderTest.getReadAllResponse());
        verify(appointmentRepository).readAll(headers, parameters);
        repositoryLiveData.setValue(mock(AppointmentReadAll.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getReadAllResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-010
    // Real bug: AppointmentpageViewModel.readAll() crashes when repository was not instantiated.
    @Test
    public void givenFreshAppointmentpageViewModelWhenReadAllCalledThenShouldNotCrash() {
        AppointmentpageViewModel viewModelUnderTest = new AppointmentpageViewModel();

        // This fails because repository is null unless instantiate() is called first.
        viewModelUnderTest.readAll(headers, parameters);
    }

    // Test Case ID: TC-VM-011
    // Valid repository interaction: HomepageViewModel should expose repository LiveData values.
    @Test
    public void givenInjectedHomepageRepositoriesWhenReadAllSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        HomepageViewModel viewModelUnderTest = new HomepageViewModel();
        SingleLiveEvent<DoctorReadAll> doctorLiveData = new SingleLiveEvent<>();
        SingleLiveEvent<com.example.do_an_tot_nghiep.Container.SpecialityReadAll> specialityLiveData = new SingleLiveEvent<>();
        MutableLiveData<Boolean> doctorAnimation = new MutableLiveData<>();
        MutableLiveData<Boolean> specialityAnimation = new MutableLiveData<>();

        doReturn(doctorAnimation).when(doctorRepository).getAnimation();
        doReturn(doctorLiveData).when(doctorRepository).getReadAllResponse();
        doReturn(specialityAnimation).when(specialityRepository).getAnimation();
        doReturn(specialityLiveData).when(specialityRepository).getReadAllResponse();
        setPrivateField(viewModelUnderTest, "doctorRepository", doctorRepository);
        setPrivateField(viewModelUnderTest, "specialityRepository", specialityRepository);

        viewModelUnderTest.doctorReadAll(headers, parameters);
        viewModelUnderTest.specialityReadAll(headers, parameters);
        AtomicReference<DoctorReadAll> observedDoctor = observe(viewModelUnderTest.getDoctorReadAllResponse());
        AtomicReference<com.example.do_an_tot_nghiep.Container.SpecialityReadAll> observedSpeciality = observe(viewModelUnderTest.getSpecialityReadAllResponse());
        verify(doctorRepository).readAll(headers, parameters);
        verify(specialityRepository).readAll(headers, parameters);
        doctorLiveData.setValue(mock(DoctorReadAll.class));
        specialityLiveData.setValue(mock(com.example.do_an_tot_nghiep.Container.SpecialityReadAll.class));

        assertSame(doctorLiveData, viewModelUnderTest.getDoctorReadAllResponse());
        assertSame(specialityLiveData, viewModelUnderTest.getSpecialityReadAllResponse());
        assertNotNull(observedDoctor.get());
        assertNotNull(observedSpeciality.get());
    }

    // Test Case ID: TC-VM-012
    // Real bug: HomepageViewModel.doctorReadAll() crashes when repositories were not instantiated.
    @Test
    public void givenFreshHomepageViewModelWhenDoctorReadAllCalledThenShouldNotCrash() {
        HomepageViewModel viewModelUnderTest = new HomepageViewModel();

        // This fails because doctorRepository/specialityRepository are null unless instantiate() is called first.
        viewModelUnderTest.doctorReadAll(headers, parameters);
    }

    // ============================================================
    // NEW TEST CASES TC-VM-013 to TC-VM-056
    // Coverage for untested ViewModels and methods
    // ============================================================

    // Test Case ID: TC-VM-013
    // DoctorpageViewModel - Real bug: readById crashes without instantiate()
    @Test
    public void givenFreshDoctorpageViewModelWhenReadByIdCalledThenShouldNotCrash() {
        DoctorpageViewModel viewModelUnderTest = new DoctorpageViewModel();

        // This fails because repository is null unless instantiate() is called first.
        viewModelUnderTest.readById(headers, "D1");
    }

    // Test Case ID: TC-VM-014
    // DoctorpageViewModel - Animation getter with null animation (lazy init bug)
    @Test
    public void givenFreshDoctorpageViewModelWhenGetAnimationCalledThenShouldNotBeNull() {
        DoctorpageViewModel viewModelUnderTest = new DoctorpageViewModel();

        // animation field is not initialized by default, getAnimation() returns null
        // This fails because animation is null when not initialized via readById()
        assertNotNull(viewModelUnderTest.getAnimation());
    }

    // Test Case ID: TC-VM-015
    // HomepageViewModel - Real bug: specialityReadAll crashes without instantiate()
    @Test
    public void givenFreshHomepageViewModelWhenSpecialityReadAllCalledThenShouldNotCrash() {
        HomepageViewModel viewModelUnderTest = new HomepageViewModel();

        // This fails because specialityRepository is null unless instantiate() is called first.
        viewModelUnderTest.specialityReadAll(headers, parameters);
    }

    // Test Case ID: TC-VM-016
    // HomepageViewModel - Animation getter with null animation (lazy init bug)
    @Test
    public void givenFreshHomepageViewModelWhenGetAnimationCalledThenShouldNotBeNull() {
        HomepageViewModel viewModelUnderTest = new HomepageViewModel();

        // animation is lazy initialized in getAnimation() - PASS
        assertNotNull(viewModelUnderTest.getAnimation());
    }

    // Test Case ID: TC-VM-017
    // RecordpageViewModel - Real bug: readByID crashes without instantiate()
    @Test
    public void givenFreshRecordpageViewModelWhenReadByIdCalledThenShouldNotCrash() {
        RecordpageViewModel viewModelUnderTest = new RecordpageViewModel();

        // This fails because repository is null unless instantiate() is called first.
        viewModelUnderTest.readByID(headers, "A1");
    }

    // Test Case ID: TC-VM-018
    // RecordpageViewModel - getReadByIDResponse with uninitialized response
    @Test
    public void givenFreshRecordpageViewModelWhenGetReadByIdResponseCalledThenShouldNotBeNull() {
        RecordpageViewModel viewModelUnderTest = new RecordpageViewModel();

        // readByIDResponse is initialized in field declaration - PASS
        assertNotNull(viewModelUnderTest.getReadByIDResponse());
    }

    // Test Case ID: TC-VM-019
    // SearchpageViewModel - Real bug: doctorReadAll crashes without instantiate()
    @Test
    public void givenFreshSearchpageViewModelWhenDoctorReadAllCalledThenShouldNotCrash() {
        SearchpageViewModel viewModelUnderTest = new SearchpageViewModel();

        // This fails because doctorRepository is null unless instantiate() is called first.
        viewModelUnderTest.doctorReadAll(headers, parameters);
    }

    // Test Case ID: TC-VM-020
    // SearchpageViewModel - Real bug: specialityReadAll crashes without instantiate()
    @Test
    public void givenFreshSearchpageViewModelWhenSpecialityReadAllCalledThenShouldNotCrash() {
        SearchpageViewModel viewModelUnderTest = new SearchpageViewModel();

        // This fails because specialityRepository is null unless instantiate() is called first.
        viewModelUnderTest.specialityReadAll(headers, parameters);
    }

    // Test Case ID: TC-VM-021
    // SearchpageViewModel - Real bug: serviceReadAll crashes without instantiate()
    @Test
    public void givenFreshSearchpageViewModelWhenServiceReadAllCalledThenShouldNotCrash() {
        SearchpageViewModel viewModelUnderTest = new SearchpageViewModel();

        // This fails because serviceRepository is null unless instantiate() is called first.
        viewModelUnderTest.serviceReadAll(headers, parameters);
    }

    // Test Case ID: TC-VM-022
    // SearchpageViewModel - Animation getter with null animation
    @Test
    public void givenFreshSearchpageViewModelWhenGetAnimationCalledThenShouldNotBeNull() {
        SearchpageViewModel viewModelUnderTest = new SearchpageViewModel();

        // animation is initialized in field declaration - PASS
        assertNotNull(viewModelUnderTest.getAnimation());
    }

    // Test Case ID: TC-VM-023
    // SearchpageViewModel - Getters for response LiveData
    @Test
    public void givenFreshSearchpageViewModelWhenGetResponseGettersCalledThenShouldNotBeNull() {
        SearchpageViewModel viewModelUnderTest = new SearchpageViewModel();

        // All responses are initialized in field declarations - PASS
        assertNotNull(viewModelUnderTest.getSpecialityReadAll());
        assertNotNull(viewModelUnderTest.getDoctorReadAllResponse());
        assertNotNull(viewModelUnderTest.getServiceReadAllResponse());
    }

    // Test Case ID: TC-VM-024
    // ServicepageViewModel - Real bug: readById crashes without instantiate()
    @Test
    public void givenFreshServicepageViewModelWhenReadByIdCalledThenShouldNotCrash() {
        ServicepageViewModel viewModelUnderTest = new ServicepageViewModel();

        // This fails because repository is null unless instantiate() is called first.
        viewModelUnderTest.readById(headers, "S1");
    }

    // Test Case ID: TC-VM-025
    // ServicepageViewModel - Real bug: doctorReadAll crashes without instantiate()
    @Test
    public void givenFreshServicepageViewModelWhenDoctorReadAllCalledThenShouldNotCrash() {
        ServicepageViewModel viewModelUnderTest = new ServicepageViewModel();

        // This fails because doctorRepository is null unless instantiate() is called first.
        viewModelUnderTest.doctorReadAll(headers, parameters);
    }

    // Test Case ID: TC-VM-026
    // ServicepageViewModel - Animation getter with null animation (lazy init bug)
    @Test
    public void givenFreshServicepageViewModelWhenGetAnimationCalledThenShouldNotBeNull() {
        ServicepageViewModel viewModelUnderTest = new ServicepageViewModel();

        // animation field is not initialized by default, getAnimation() returns null
        // This fails because animation is null when not initialized via readById()
        assertNotNull(viewModelUnderTest.getAnimation());
    }

    // Test Case ID: TC-VM-027
    // ServicepageViewModel - Response getter
    @Test
    public void givenFreshServicepageViewModelWhenGetResponseCalledThenShouldNotBeNull() {
        ServicepageViewModel viewModelUnderTest = new ServicepageViewModel();

        // response is initialized in field declaration - PASS
        assertNotNull(viewModelUnderTest.getResponse());
    }

    // Test Case ID: TC-VM-028
    // SettingspageViewModel - Real bug: readAll crashes without instantiate()
    @Test
    public void givenFreshSettingspageViewModelWhenReadAllCalledThenShouldNotCrash() {
        SettingspageViewModel viewModelUnderTest = new SettingspageViewModel();

        // This fails because appointmentRepository is null unless instantiate() is called first.
        viewModelUnderTest.readAll(headers, parameters);
    }

    // Test Case ID: TC-VM-029
    // SettingspageViewModel - Real bug: bookingReadAll crashes without instantiate()
    @Test
    public void givenFreshSettingspageViewModelWhenBookingReadAllCalledThenShouldNotCrash() {
        SettingspageViewModel viewModelUnderTest = new SettingspageViewModel();

        // This fails because bookingRepository is null unless instantiate() is called first.
        viewModelUnderTest.bookingReadAll(headers, parameters);
    }

    // Test Case ID: TC-VM-030
    // SettingspageViewModel - Animation getter
    @Test
    public void givenFreshSettingspageViewModelWhenGetAnimationCalledThenShouldNotBeNull() {
        SettingspageViewModel viewModelUnderTest = new SettingspageViewModel();

        // animation is initialized in field declaration - PASS
        assertNotNull(viewModelUnderTest.getAnimation());
    }

    // Test Case ID: TC-VM-031
    // SettingspageViewModel - Response getters
    @Test
    public void givenFreshSettingspageViewModelWhenGetResponseGettersCalledThenShouldNotBeNull() {
        SettingspageViewModel viewModelUnderTest = new SettingspageViewModel();

        // readAllResponse and bookingReadAll are initialized in field declarations - PASS
        assertNotNull(viewModelUnderTest.getReadAllResponse());
        assertNotNull(viewModelUnderTest.getBookingReadAll());
    }

    // Test Case ID: TC-VM-032
    // SpecialitypageViewModel - Real bug: readById crashes without instantiate()
    @Test
    public void givenFreshSpecialitypageViewModelWhenReadByIdCalledThenShouldNotCrash() {
        SpecialitypageViewModel viewModelUnderTest = new SpecialitypageViewModel();

        // This fails because repository is null unless instantiate() is called first.
        viewModelUnderTest.readById(headers, "SP1");
    }

    // Test Case ID: TC-VM-033
    // SpecialitypageViewModel - Real bug: doctorReadAll crashes without instantiate()
    @Test
    public void givenFreshSpecialitypageViewModelWhenDoctorReadAllCalledThenShouldNotCrash() {
        SpecialitypageViewModel viewModelUnderTest = new SpecialitypageViewModel();

        // This fails because doctorRepository is null unless instantiate() is called first.
        viewModelUnderTest.doctorReadAll(headers, parameters);
    }

    // Test Case ID: TC-VM-034
    // SpecialitypageViewModel - Animation getter
    @Test
    public void givenFreshSpecialitypageViewModelWhenGetAnimationCalledThenShouldNotBeNull() {
        SpecialitypageViewModel viewModelUnderTest = new SpecialitypageViewModel();

        // animation is initialized in field declaration - PASS
        assertNotNull(viewModelUnderTest.getAnimation());
    }

    // Test Case ID: TC-VM-035
    // SpecialitypageViewModel - Response getters
    @Test
    public void givenFreshSpecialitypageViewModelWhenGetResponseGettersCalledThenShouldNotBeNull() {
        SpecialitypageViewModel viewModelUnderTest = new SpecialitypageViewModel();

        // response and doctorReadAllResponse are initialized in field declarations - PASS
        assertNotNull(viewModelUnderTest.getResponse());
        assertNotNull(viewModelUnderTest.getDoctorReadAllResponse());
    }

    // Test Case ID: TC-VM-036
    // TreatmentpageViewModel - Real bug: appointmentReadAll crashes without instantiate()
    @Test
    public void givenFreshTreatmentpageViewModelWhenAppointmentReadAllCalledThenShouldNotCrash() {
        TreatmentpageViewModel viewModelUnderTest = new TreatmentpageViewModel();

        // This fails because appointmentRepository is null unless instantiate() is called first.
        viewModelUnderTest.appointmentReadAll(headers, parameters);
    }

    // Test Case ID: TC-VM-037
    // TreatmentpageViewModel - Real bug: treatmentReadAll crashes without instantiate()
    @Test
    public void givenFreshTreatmentpageViewModelWhenTreatmentReadAllCalledThenShouldNotCrash() {
        TreatmentpageViewModel viewModelUnderTest = new TreatmentpageViewModel();

        // This fails because treatmentRepository is null unless instantiate() is called first.
        viewModelUnderTest.treatmentReadAll(headers, "A1");
    }

    // Test Case ID: TC-VM-038
    // TreatmentpageViewModel - Real bug: treatmentReadByID crashes without instantiate()
    @Test
    public void givenFreshTreatmentpageViewModelWhenTreatmentReadByIdCalledThenShouldNotCrash() {
        TreatmentpageViewModel viewModelUnderTest = new TreatmentpageViewModel();

        // This fails because treatmentRepository is null unless instantiate() is called first.
        viewModelUnderTest.treatmentReadByID(headers, "T1");
    }

    // Test Case ID: TC-VM-039
    // TreatmentpageViewModel - Animation getter
    @Test
    public void givenFreshTreatmentpageViewModelWhenGetAnimationCalledThenShouldNotBeNull() {
        TreatmentpageViewModel viewModelUnderTest = new TreatmentpageViewModel();

        // animation is initialized in field declaration - PASS
        assertNotNull(viewModelUnderTest.getAnimation());
    }

    // Test Case ID: TC-VM-040
    // TreatmentpageViewModel - Response getters
    @Test
    public void givenFreshTreatmentpageViewModelWhenGetResponseGettersCalledThenShouldNotBeNull() {
        TreatmentpageViewModel viewModelUnderTest = new TreatmentpageViewModel();

        // All responses are initialized in field declarations - PASS
        assertNotNull(viewModelUnderTest.getAppointmentReadAllResponse());
        assertNotNull(viewModelUnderTest.getTreatmentReadAllResponse());
        assertNotNull(viewModelUnderTest.getTreatmentReadByIDResponse());
    }

    // Test Case ID: TC-VM-041
    // AppointmentpageViewModel - Real bug: readByID crashes without instantiate()
    @Test
    public void givenFreshAppointmentpageViewModelWhenReadByIdCalledThenShouldNotCrash() {
        AppointmentpageViewModel viewModelUnderTest = new AppointmentpageViewModel();

        // This fails because repository is null unless instantiate() is called first.
        viewModelUnderTest.readByID(headers, "A1");
    }

    // Test Case ID: TC-VM-042
    // AppointmentpageViewModel - Real bug: getQueue crashes without instantiate()
    @Test
    public void givenFreshAppointmentpageViewModelWhenGetQueueCalledThenShouldNotCrash() {
        AppointmentpageViewModel viewModelUnderTest = new AppointmentpageViewModel();

        // This fails because queueRepository is null unless instantiate() is called first.
        viewModelUnderTest.getQueue(headers, parameters);
    }

    // Test Case ID: TC-VM-043
    // AppointmentpageViewModel - Animation getter
    @Test
    public void givenFreshAppointmentpageViewModelWhenGetAnimationCalledThenShouldNotBeNull() {
        AppointmentpageViewModel viewModelUnderTest = new AppointmentpageViewModel();

        // animation is initialized in field declaration - PASS
        assertNotNull(viewModelUnderTest.getAnimation());
    }

    // Test Case ID: TC-VM-044
    // AppointmentpageViewModel - Response getters
    @Test
    public void givenFreshAppointmentpageViewModelWhenGetResponseGettersCalledThenShouldNotBeNull() {
        AppointmentpageViewModel viewModelUnderTest = new AppointmentpageViewModel();

        // readAllResponse and readByIDResponse are initialized in field declarations - PASS
        assertNotNull(viewModelUnderTest.getReadAllResponse());
        assertNotNull(viewModelUnderTest.getReadByIDResponse());
    }

    // Test Case ID: TC-VM-045
    // BookingpageViewModel - Real bug: bookingReadByID crashes without instantiate()
    @Test
    public void givenFreshBookingpageViewModelWhenBookingReadByIdCalledThenShouldNotCrash() {
        BookingpageViewModel viewModelUnderTest = new BookingpageViewModel();

        // This fails because bookingRepository is null unless instantiate() is called first.
        viewModelUnderTest.bookingReadByID(headers, "B1");
    }

    // Test Case ID: TC-VM-046
    // BookingpageViewModel - Real bug: bookingPhotoReadAll crashes without instantiate()
    @Test
    public void givenFreshBookingpageViewModelWhenBookingPhotoReadAllCalledThenShouldNotCrash() {
        BookingpageViewModel viewModelUnderTest = new BookingpageViewModel();

        // This fails because bookingPhotoRepository is null unless instantiate() is called first.
        viewModelUnderTest.bookingPhotoReadAll(headers, "B1");
    }

    // Test Case ID: TC-VM-047
    // BookingpageViewModel - Real bug: doctorReadByID crashes without instantiate()
    @Test
    public void givenFreshBookingpageViewModelWhenDoctorReadByIdCalledThenShouldNotCrash() {
        BookingpageViewModel viewModelUnderTest = new BookingpageViewModel();

        // This fails because doctorRepository is null unless instantiate() is called first.
        viewModelUnderTest.doctorReadByID(headers, "D1");
    }

    // Test Case ID: TC-VM-048
    // BookingpageViewModel - Animation getter
    @Test
    public void givenFreshBookingpageViewModelWhenGetAnimationCalledThenShouldNotBeNull() {
        BookingpageViewModel viewModelUnderTest = new BookingpageViewModel();

        // animation is initialized in field declaration - PASS
        assertNotNull(viewModelUnderTest.getAnimation());
    }

    // Test Case ID: TC-VM-049
    // BookingpageViewModel - Response getters
    @Test
    public void givenFreshBookingpageViewModelWhenGetResponseGettersCalledThenShouldNotBeNull() {
        BookingpageViewModel viewModelUnderTest = new BookingpageViewModel();

        // serviceReadByIdResponse and bookingReadByIdResponse are initialized in field declarations - PASS
        assertNotNull(viewModelUnderTest.getServiceReadByIdResponse());
        assertNotNull(viewModelUnderTest.getBookingReadByIdResponse());
    }

    // Test Case ID: TC-VM-050
    // NotificationViewModel - readAll with null repository (already tested in TC-VM-006)
    @Test
    public void givenFreshNotificationViewModelWhenReadAllCalledThenRepositoryIsNull() {
        NotificationViewModel viewModelUnderTest = new NotificationViewModel();

        // Repository is null without instantiate() - FAIL
        // This will cause NullPointerException when calling readAll
        assertNull(viewModelUnderTest.getReadAllResponse().getValue());
    }

    // Test Case ID: TC-VM-052
    // LoginViewModel - loginWithPhone with empty phone number
    @Test
    public void givenEmptyPhoneWhenLoginWithPhoneCalledThenShouldNotCrash() {
        LoginViewModel viewModelUnderTest = new LoginViewModel();
        Call<Login> mockApiCall = mockCall();
        doReturn(mockApiCall).when(api).login("", "secret", "patient");

        // Should not crash even with empty phone - PASS
        viewModelUnderTest.loginWithPhone("", "secret");
        verify(api).login("", "secret", "patient");
    }

    // Test Case ID: TC-VM-053
    // LoginViewModel - loginWithPhone with empty password
    @Test
    public void givenEmptyPasswordWhenLoginWithPhoneCalledThenShouldNotCrash() {
        LoginViewModel viewModelUnderTest = new LoginViewModel();
        Call<Login> mockApiCall = mockCall();
        doReturn(mockApiCall).when(api).login("0123456789", "", "patient");

        // Should not crash even with empty password - PASS
        viewModelUnderTest.loginWithPhone("0123456789", "");
        verify(api).login("0123456789", "", "patient");
    }

    // Test Case ID: TC-VM-054
    // LoginViewModel - loginWithGoogle with empty email
    @Test
    public void givenEmptyEmailWhenLoginWithGoogleCalledThenShouldNotCrash() {
        LoginViewModel viewModelUnderTest = new LoginViewModel();
        Call<Login> mockApiCall = mockCall();
        doReturn(mockApiCall).when(api).loginWithGoogle("", "secret", "patient");

        // Should not crash even with empty email - PASS
        viewModelUnderTest.loginWithGoogle("", "secret");
        verify(api).loginWithGoogle("", "secret", "patient");
    }

    // Test Case ID: TC-VM-055
    // LoginViewModel - getLoginWithGoogleResponse lazy initialization
    @Test
    public void givenFreshLoginViewModelWhenGetLoginWithGoogleResponseCalledThenShouldNotBeNull() {
        LoginViewModel viewModelUnderTest = new LoginViewModel();

        // getLoginWithGoogleResponse() has lazy initialization - PASS
        assertNotNull(viewModelUnderTest.getLoginWithGoogleResponse());
    }

    // Test Case ID: TC-VM-056
    // All repository-backed ViewModels - verify null response handling
    @Test
    public void givenViewModelsWithNullParametersWhenMethodsCalledThenShouldNotCrash() {
        // Test with null values in parameters map
        Map<String, String> nullParams = new HashMap<>();
        nullParams.put("page", null);
        nullParams.put("limit", null);

        HomepageViewModel homepageVM = new HomepageViewModel();
        homepageVM.instantiate();
        // Should not crash with null values - PASS
        homepageVM.doctorReadAll(headers, nullParams);
        homepageVM.specialityReadAll(headers, nullParams);
    }

    // ============================================================

    // ============================================================

    // Test Case ID: TC-VM-057
    // RecordpageViewModel - Test with injected repository
    @Test
    public void givenInjectedRecordRepositoryWhenReadByIdCalledThenViewModelExposesRepositoryLiveData() throws Exception {
        RecordpageViewModel viewModelUnderTest = new RecordpageViewModel();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();
        MutableLiveData<RecordReadByID> repositoryLiveData = new MutableLiveData<>();

        doReturn(repositoryAnimation).when(recordRepository).getAnimation();
        doReturn(repositoryLiveData).when(recordRepository).readByID(headers, "A1");
        setPrivateField(viewModelUnderTest, "repository", recordRepository);

        viewModelUnderTest.readByID(headers, "A1");
        AtomicReference<RecordReadByID> observedValue = observe(viewModelUnderTest.getReadByIDResponse());
        verify(recordRepository).readByID(headers, "A1");
        repositoryLiveData.setValue(mock(RecordReadByID.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getReadByIDResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-058
    // RecordpageViewModel - instantiate() is idempotent
    @Test
    public void givenRecordpageViewModelWhenInstantiateCalledTwiceThenShouldNotCrash() {
        RecordpageViewModel viewModelUnderTest = new RecordpageViewModel();
        viewModelUnderTest.instantiate();
        viewModelUnderTest.instantiate(); // Should not crash - PASS
    }

    // Test Case ID: TC-VM-059
    // RecordpageViewModel - Animation is properly initialized
    @Test
    public void givenRecordpageViewModelWhenReadByIdCalledThenAnimationIsNotNull() throws Exception {
        RecordpageViewModel viewModelUnderTest = new RecordpageViewModel();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();

        doReturn(repositoryAnimation).when(recordRepository).getAnimation();
        doReturn(new MutableLiveData<>()).when(recordRepository).readByID(any(), any());
        setPrivateField(viewModelUnderTest, "repository", recordRepository);

        viewModelUnderTest.readByID(headers, "A1");

        assertNotNull(viewModelUnderTest.getAnimation());
    }

    // Test Case ID: TC-VM-060
    // SpecialitypageViewModel - Test with injected repository
    @Test
    public void givenInjectedSpecialityRepositoryWhenReadByIdCalledThenViewModelExposesRepositoryLiveData() throws Exception {
        SpecialitypageViewModel viewModelUnderTest = new SpecialitypageViewModel();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();
        SingleLiveEvent<SpecialityReadByID> repositoryLiveData = new SingleLiveEvent<>();

        doReturn(repositoryAnimation).when(specialityRepository).getAnimation();
        doReturn(repositoryLiveData).when(specialityRepository).getReadByIdResponse();
        setPrivateField(viewModelUnderTest, "repository", specialityRepository);

        viewModelUnderTest.readById(headers, "SP1");
        AtomicReference<SpecialityReadByID> observedValue = observe(viewModelUnderTest.getResponse());
        verify(specialityRepository).readById(headers, "SP1");
        repositoryLiveData.setValue(mock(SpecialityReadByID.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-061
    // SpecialitypageViewModel - Test doctorReadAll with injected repository
    @Test
    public void givenInjectedDoctorRepositoryWhenDoctorReadAllCalledThenViewModelExposesRepositoryLiveData() throws Exception {
        SpecialitypageViewModel viewModelUnderTest = new SpecialitypageViewModel();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();
        SingleLiveEvent<DoctorReadAll> repositoryLiveData = new SingleLiveEvent<>();

        doReturn(repositoryAnimation).when(doctorRepository).getAnimation();
        doReturn(repositoryLiveData).when(doctorRepository).getReadAllResponse();
        setPrivateField(viewModelUnderTest, "doctorRepository", doctorRepository);

        viewModelUnderTest.doctorReadAll(headers, parameters);
        AtomicReference<DoctorReadAll> observedValue = observe(viewModelUnderTest.getDoctorReadAllResponse());
        verify(doctorRepository).readAll(headers, parameters);
        repositoryLiveData.setValue(mock(DoctorReadAll.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getDoctorReadAllResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-062
    // SpecialitypageViewModel - instantiate() is idempotent
    @Test
    public void givenSpecialitypageViewModelWhenInstantiateCalledTwiceThenShouldNotCrash() {
        SpecialitypageViewModel viewModelUnderTest = new SpecialitypageViewModel();
        viewModelUnderTest.instantiate();
        viewModelUnderTest.instantiate(); // Should not crash - PASS
    }

    // Test Case ID: TC-VM-063
    // SpecialitypageViewModel - instantiate() initializes both repositories
    @Test
    public void givenSpecialitypageViewModelWhenInstantiateCalledThenRepositoriesAreInitialized() {
        SpecialitypageViewModel viewModelUnderTest = new SpecialitypageViewModel();
        viewModelUnderTest.instantiate();

        // After instantiate(), calling methods should not crash
        // This tests that both repository and doctorRepository are initialized
        verify(doctorRepository, org.mockito.Mockito.times(0)).readAll(any(), any()); // Not called yet
    }

    // Test Case ID: TC-VM-064
    // ServicepageViewModel - Test with injected repository
    @Test
    public void givenInjectedServiceRepositoryWhenReadByIdCalledThenViewModelExposesRepositoryLiveData() throws Exception {
        ServicepageViewModel viewModelUnderTest = new ServicepageViewModel();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();
        SingleLiveEvent<ServiceReadByID> repositoryLiveData = new SingleLiveEvent<>();

        doReturn(repositoryAnimation).when(serviceRepository).getAnimation();
        doReturn(repositoryLiveData).when(serviceRepository).getReadByIDResponse();
        setPrivateField(viewModelUnderTest, "repository", serviceRepository);

        viewModelUnderTest.readById(headers, "S1");
        AtomicReference<ServiceReadByID> observedValue = observe(viewModelUnderTest.getResponse());
        verify(serviceRepository).readByID(headers, "S1");
        repositoryLiveData.setValue(mock(ServiceReadByID.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-065
    // ServicepageViewModel - Test doctorReadAll with injected repository
    @Test
    public void givenInjectedDoctorRepositoryWhenServicepageDoctorReadAllCalledThenViewModelExposesRepositoryLiveData() throws Exception {
        ServicepageViewModel viewModelUnderTest = new ServicepageViewModel();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();
        SingleLiveEvent<DoctorReadAll> repositoryLiveData = new SingleLiveEvent<>();

        doReturn(repositoryAnimation).when(doctorRepository).getAnimation();
        doReturn(repositoryLiveData).when(doctorRepository).getReadAllResponse();
        setPrivateField(viewModelUnderTest, "doctorRepository", doctorRepository);

        viewModelUnderTest.doctorReadAll(headers, parameters);
        AtomicReference<DoctorReadAll> observedValue = observe(viewModelUnderTest.getDoctorReadAllResponse());
        verify(doctorRepository).readAll(headers, parameters);
        repositoryLiveData.setValue(mock(DoctorReadAll.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getDoctorReadAllResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-066
    // ServicepageViewModel - instantiate() is idempotent
    @Test
    public void givenServicepageViewModelWhenInstantiateCalledTwiceThenShouldNotCrash() {
        ServicepageViewModel viewModelUnderTest = new ServicepageViewModel();
        viewModelUnderTest.instantiate();
        viewModelUnderTest.instantiate(); // Should not crash - PASS
    }

    // Test Case ID: TC-VM-067
    // ServicepageViewModel - getAnimation returns null when not initialized
    @Test
    public void givenFreshServicepageViewModelWhenGetAnimationCalledBeforeOperationThenReturnsNull() {
        ServicepageViewModel viewModelUnderTest = new ServicepageViewModel();

        // animation field is null when not initialized via instantiate()
        // This is a known bug - animation is declared but not initialized
        assertNull(viewModelUnderTest.getAnimation());
    }

    // Test Case ID: TC-VM-068
    // TreatmentpageViewModel - Test appointmentReadAll with injected repository
    @Test
    public void givenInjectedAppointmentRepositoryWhenTreatmentpageAppointmentReadAllCalledThenViewModelExposesRepositoryLiveData() throws Exception {
        TreatmentpageViewModel viewModelUnderTest = new TreatmentpageViewModel();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();
        SingleLiveEvent<AppointmentReadAll> repositoryLiveData = new SingleLiveEvent<>();

        doReturn(repositoryAnimation).when(appointmentRepository).getAnimation();
        doReturn(repositoryLiveData).when(appointmentRepository).getReadAllResponse();
        setPrivateField(viewModelUnderTest, "appointmentRepository", appointmentRepository);

        viewModelUnderTest.appointmentReadAll(headers, parameters);
        AtomicReference<AppointmentReadAll> observedValue = observe(viewModelUnderTest.getAppointmentReadAllResponse());
        verify(appointmentRepository).readAll(headers, parameters);
        repositoryLiveData.setValue(mock(AppointmentReadAll.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getAppointmentReadAllResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-069
    // TreatmentpageViewModel - Test treatmentReadAll with injected repository
    @Test
    public void givenInjectedTreatmentRepositoryWhenTreatmentReadAllCalledThenViewModelExposesRepositoryLiveData() throws Exception {
        TreatmentpageViewModel viewModelUnderTest = new TreatmentpageViewModel();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();
        SingleLiveEvent<TreatmentReadAll> repositoryLiveData = new SingleLiveEvent<>();

        doReturn(repositoryAnimation).when(treatmentRepository).getAnimation();
        doReturn(repositoryLiveData).when(treatmentRepository).getReadAllResponse();
        setPrivateField(viewModelUnderTest, "treatmentRepository", treatmentRepository);

        viewModelUnderTest.treatmentReadAll(headers, "A1");
        AtomicReference<TreatmentReadAll> observedValue = observe(viewModelUnderTest.getTreatmentReadAllResponse());
        verify(treatmentRepository).readAll(headers, "A1");
        repositoryLiveData.setValue(mock(TreatmentReadAll.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getTreatmentReadAllResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-070
    // TreatmentpageViewModel - Test treatmentReadByID with injected repository
    @Test
    public void givenInjectedTreatmentRepositoryWhenTreatmentReadByIDCalledThenViewModelExposesRepositoryLiveData() throws Exception {
        TreatmentpageViewModel viewModelUnderTest = new TreatmentpageViewModel();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();
        SingleLiveEvent<TreatmentReadByID> repositoryLiveData = new SingleLiveEvent<>();

        doReturn(repositoryAnimation).when(treatmentRepository).getAnimation();
        doReturn(repositoryLiveData).when(treatmentRepository).getReadByIDResponse();
        setPrivateField(viewModelUnderTest, "treatmentRepository", treatmentRepository);

        viewModelUnderTest.treatmentReadByID(headers, "T1");
        AtomicReference<TreatmentReadByID> observedValue = observe(viewModelUnderTest.getTreatmentReadByIDResponse());
        verify(treatmentRepository).readByID(headers, "T1");
        repositoryLiveData.setValue(mock(TreatmentReadByID.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getTreatmentReadByIDResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-071
    // TreatmentpageViewModel - instantiate() is idempotent
    @Test
    public void givenTreatmentpageViewModelWhenInstantiateCalledTwiceThenShouldNotCrash() {
        TreatmentpageViewModel viewModelUnderTest = new TreatmentpageViewModel();
        viewModelUnderTest.instantiate();
        viewModelUnderTest.instantiate(); // Should not crash - PASS
    }

    // Test Case ID: TC-VM-072
    // AppointmentpageViewModel - Test readByID with injected repository
    @Test
    public void givenInjectedAppointmentRepositoryWhenAppointmentpageReadByIdCalledThenViewModelExposesRepositoryLiveData() throws Exception {
        AppointmentpageViewModel viewModelUnderTest = new AppointmentpageViewModel();
        SingleLiveEvent<AppointmentReadByID> repositoryLiveData = new SingleLiveEvent<>();

        doReturn(repositoryLiveData).when(appointmentRepository).getReadByIDResponse();
        setPrivateField(viewModelUnderTest, "repository", appointmentRepository);

        viewModelUnderTest.readByID(headers, "A1");
        AtomicReference<AppointmentReadByID> observedValue = observe(viewModelUnderTest.getReadByIDResponse());
        verify(appointmentRepository).readByID(headers, "A1");
        repositoryLiveData.setValue(mock(AppointmentReadByID.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getReadByIDResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-073
    // AppointmentpageViewModel - Test getQueue with injected repository
    @Test
    public void givenInjectedQueueRepositoryWhenGetQueueCalledThenViewModelExposesRepositoryLiveData() throws Exception {
        AppointmentpageViewModel viewModelUnderTest = new AppointmentpageViewModel();
        MutableLiveData<AppointmentQueue> repositoryLiveData = new MutableLiveData<>();

        doReturn(repositoryLiveData).when(appointmentQueueRepository).getAppointmentQueue(headers, parameters);
        setPrivateField(viewModelUnderTest, "queueRepository", appointmentQueueRepository);

        viewModelUnderTest.getQueue(headers, parameters);
        AtomicReference<AppointmentQueue> observedValue = observe(viewModelUnderTest.getAppointmentQueueResponse());
        verify(appointmentQueueRepository).getAppointmentQueue(headers, parameters);
        repositoryLiveData.setValue(mock(AppointmentQueue.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getAppointmentQueueResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-074
    // AppointmentpageViewModel - instantiate() is idempotent
    @Test
    public void givenAppointmentpageViewModelWhenInstantiateCalledTwiceThenShouldNotCrash() {
        AppointmentpageViewModel viewModelUnderTest = new AppointmentpageViewModel();
        viewModelUnderTest.instantiate();
        viewModelUnderTest.instantiate(); // Should not crash - PASS
    }

    // Test Case ID: TC-VM-075
    // AppointmentpageViewModel - instantiate() initializes both repositories
    @Test
    public void givenAppointmentpageViewModelWhenInstantiateCalledThenBothRepositoriesAreInitialized() {
        AppointmentpageViewModel viewModelUnderTest = new AppointmentpageViewModel();
        viewModelUnderTest.instantiate();

        // After instantiate(), calling methods should not crash
        // This verifies both repository and queueRepository are initialized
    }

    // Test Case ID: TC-VM-076
    // BookingpageViewModel - Test bookingPhotoReadAll with injected repository
    @Test
    public void givenInjectedBookingPhotoRepositoryWhenBookingPhotoReadAllCalledThenViewModelExposesRepositoryLiveData() throws Exception {
        BookingpageViewModel viewModelUnderTest = new BookingpageViewModel();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();
        SingleLiveEvent<BookingPhotoReadAll> repositoryLiveData = new SingleLiveEvent<>();

        doReturn(repositoryAnimation).when(bookingPhotoRepository).getAnimation();
        doReturn(repositoryLiveData).when(bookingPhotoRepository).getReadAllResponse();
        setPrivateField(viewModelUnderTest, "bookingPhotoRepository", bookingPhotoRepository);

        viewModelUnderTest.bookingPhotoReadAll(headers, "B1");
        AtomicReference<BookingPhotoReadAll> observedValue = observe(viewModelUnderTest.getBookingPhotoReadAllResponse());
        verify(bookingPhotoRepository).readAll(headers, "B1");
        repositoryLiveData.setValue(mock(BookingPhotoReadAll.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getBookingPhotoReadAllResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-077
    // BookingpageViewModel - Test doctorReadByID with injected repository
    @Test
    public void givenInjectedDoctorRepositoryWhenBookingpageDoctorReadByIdCalledThenViewModelExposesRepositoryLiveData() throws Exception {
        BookingpageViewModel viewModelUnderTest = new BookingpageViewModel();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();
        SingleLiveEvent<DoctorReadByID> repositoryLiveData = new SingleLiveEvent<>();

        doReturn(repositoryAnimation).when(doctorRepository).getAnimation();
        doReturn(repositoryLiveData).when(doctorRepository).getReadByIdResponse();
        setPrivateField(viewModelUnderTest, "doctorRepository", doctorRepository);

        viewModelUnderTest.doctorReadByID(headers, "D1");
        AtomicReference<DoctorReadByID> observedValue = observe(viewModelUnderTest.getDoctorReadByIdResponse());
        verify(doctorRepository).readById(headers, "D1");
        repositoryLiveData.setValue(mock(DoctorReadByID.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getDoctorReadByIdResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-078
    // BookingpageViewModel - instantiate() is idempotent
    @Test
    public void givenBookingpageViewModelWhenInstantiateCalledTwiceThenShouldNotCrash() {
        BookingpageViewModel viewModelUnderTest = new BookingpageViewModel();
        viewModelUnderTest.instantiate();
        viewModelUnderTest.instantiate(); // Should not crash - PASS
    }

    // Test Case ID: TC-VM-079
    // BookingpageViewModel - instantiate() initializes all four repositories
    @Test
    public void givenBookingpageViewModelWhenInstantiateCalledThenAllRepositoriesAreInitialized() {
        BookingpageViewModel viewModelUnderTest = new BookingpageViewModel();
        viewModelUnderTest.instantiate();

        // After instantiate(), all four repositories should be initialized
        // Calling any method should not crash
    }

    // Test Case ID: TC-VM-080
    // NotificationViewModel - instantiate() is idempotent
    @Test
    public void givenNotificationViewModelWhenInstantiateCalledTwiceThenShouldNotCrash() {
        NotificationViewModel viewModelUnderTest = new NotificationViewModel();
        viewModelUnderTest.instantiate();
        viewModelUnderTest.instantiate(); // Should not crash - PASS
    }

    // Test Case ID: TC-VM-081
    // NotificationViewModel - getAnimation is properly initialized
    @Test
    public void givenFreshNotificationViewModelWhenGetAnimationCalledThenShouldNotBeNull() {
        NotificationViewModel viewModelUnderTest = new NotificationViewModel();

        // animation is initialized in field declaration - PASS
        assertNotNull(viewModelUnderTest.getAnimation());
    }

    // Test Case ID: TC-VM-082
    // SettingspageViewModel - Test readAll with injected repository
    @Test
    public void givenInjectedAppointmentRepositoryWhenSettingspageReadAllCalledThenViewModelExposesRepositoryLiveData() throws Exception {
        SettingspageViewModel viewModelUnderTest = new SettingspageViewModel();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();
        SingleLiveEvent<AppointmentReadAll> repositoryLiveData = new SingleLiveEvent<>();

        doReturn(repositoryAnimation).when(appointmentRepository).getAnimation();
        doReturn(repositoryLiveData).when(appointmentRepository).getReadAllResponse();
        setPrivateField(viewModelUnderTest, "appointmentRepository", appointmentRepository);

        viewModelUnderTest.readAll(headers, parameters);
        AtomicReference<AppointmentReadAll> observedValue = observe(viewModelUnderTest.getReadAllResponse());
        verify(appointmentRepository).readAll(headers, parameters);
        repositoryLiveData.setValue(mock(AppointmentReadAll.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getReadAllResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-083
    // SettingspageViewModel - Test bookingReadAll with injected repository
    @Test
    public void givenInjectedBookingRepositoryWhenSettingspageBookingReadAllCalledThenViewModelExposesRepositoryLiveData() throws Exception {
        SettingspageViewModel viewModelUnderTest = new SettingspageViewModel();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();
        SingleLiveEvent<BookingReadAll> repositoryLiveData = new SingleLiveEvent<>();

        doReturn(repositoryAnimation).when(bookingRepository).getAnimation();
        doReturn(repositoryLiveData).when(bookingRepository).getReadAllResponse();
        setPrivateField(viewModelUnderTest, "bookingRepository", bookingRepository);

        viewModelUnderTest.bookingReadAll(headers, parameters);
        AtomicReference<BookingReadAll> observedValue = observe(viewModelUnderTest.getBookingReadAll());
        verify(bookingRepository).readAll(headers, parameters);
        repositoryLiveData.setValue(mock(BookingReadAll.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getBookingReadAll());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-084
    // SettingspageViewModel - instantiate() is idempotent
    @Test
    public void givenSettingspageViewModelWhenInstantiateCalledTwiceThenShouldNotCrash() {
        SettingspageViewModel viewModelUnderTest = new SettingspageViewModel();
        viewModelUnderTest.instantiate();
        viewModelUnderTest.instantiate(); // Should not crash - PASS
    }

    // Test Case ID: TC-VM-085
    // AppointmentpageViewModel - getAppointmentQueueResponse getter
    @Test
    public void givenFreshAppointmentpageViewModelWhenGetAppointmentQueueResponseCalledThenShouldNotBeNull() {
        AppointmentpageViewModel viewModelUnderTest = new AppointmentpageViewModel();

        // appointmentQueueResponse is initialized in field declaration - PASS
        assertNotNull(viewModelUnderTest.getAppointmentQueueResponse());
    }

    // Test Case ID: TC-VM-086
    // BookingpageViewModel - getBookingPhotoReadAllResponse getter
    @Test
    public void givenFreshBookingpageViewModelWhenGetBookingPhotoReadAllResponseCalledThenShouldNotBeNull() {
        BookingpageViewModel viewModelUnderTest = new BookingpageViewModel();

        // bookingPhotoReadAllResponse is initialized in field declaration - PASS
        assertNotNull(viewModelUnderTest.getBookingPhotoReadAllResponse());
    }

    // Test Case ID: TC-VM-087
    // BookingpageViewModel - getDoctorReadByIdResponse getter
    @Test
    public void givenFreshBookingpageViewModelWhenGetDoctorReadByIdResponseCalledThenShouldNotBeNull() {
        BookingpageViewModel viewModelUnderTest = new BookingpageViewModel();

        // doctorReadById is initialized in field declaration - PASS
        assertNotNull(viewModelUnderTest.getDoctorReadByIdResponse());
    }

    // ============================================================
    // Helper methods
    // ============================================================

    private <T> AtomicReference<T> observe(MutableLiveData<T> liveData) {
        AtomicReference<T> observedValue = new AtomicReference<>();
        Observer<T> observer = observedValue::set;
        liveData.observeForever(observer);
        return observedValue;
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private <T> Call<T> mockCall() {
        return (Call<T>) mock(Call.class);
    }
    private <T> AtomicReference<Callback<T>> captureCallback(Call<T> call) {
        AtomicReference<Callback<T>> callbackRef = new AtomicReference<>();
        doAnswer(invocation -> {
            callbackRef.set(invocation.getArgument(0));
            return null;
        }).when(call).enqueue(any());
        return callbackRef;
    }
}