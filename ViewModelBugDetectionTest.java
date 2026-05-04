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

import com.example.do_an_tot_nghiep.Appointmentpage.AppointmentpageViewModel;
import com.example.do_an_tot_nghiep.Bookingpage.BookingpageViewModel;
import com.example.do_an_tot_nghiep.Configuration.HTTPRequest;
import com.example.do_an_tot_nghiep.Container.BookingReadAll;
import com.example.do_an_tot_nghiep.Container.BookingPhotoReadAll;
import com.example.do_an_tot_nghiep.Container.RecordReadByID;
import com.example.do_an_tot_nghiep.Container.ServiceReadByID;
import com.example.do_an_tot_nghiep.Container.SpecialityReadByID;
import com.example.do_an_tot_nghiep.Container.TreatmentReadAll;
import com.example.do_an_tot_nghiep.Container.TreatmentReadByID;
import com.example.do_an_tot_nghiep.Searchpage.SearchpageViewModel;
import com.example.do_an_tot_nghiep.Servicepage.ServicepageViewModel;
import com.example.do_an_tot_nghiep.Settingspage.SettingspageViewModel;
import com.example.do_an_tot_nghiep.Specialitypage.SpecialitypageViewModel;
import com.example.do_an_tot_nghiep.Treatmentpage.TreatmentpageViewModel;
import com.example.do_an_tot_nghiep.Recordpage.RecordpageViewModel;
import com.example.do_an_tot_nghiep.Doctorpage.DoctorpageViewModel;
import com.example.do_an_tot_nghiep.Configuration.HTTPService;
import com.example.do_an_tot_nghiep.Container.AppointmentQueue;
import com.example.do_an_tot_nghiep.Container.AppointmentReadAll;
import com.example.do_an_tot_nghiep.Container.BookingReadByID;
import com.example.do_an_tot_nghiep.Container.DoctorReadAll;
import com.example.do_an_tot_nghiep.Container.DoctorReadByID;
import com.example.do_an_tot_nghiep.Container.Login;
import com.example.do_an_tot_nghiep.Container.NotificationReadAll;
import com.example.do_an_tot_nghiep.Container.PatientProfile;
import com.example.do_an_tot_nghiep.Container.ServiceReadAll;
import com.example.do_an_tot_nghiep.Helper.SingleLiveEvent;
import com.example.do_an_tot_nghiep.Homepage.HomepageViewModel;
import com.example.do_an_tot_nghiep.Loginpage.LoginViewModel;
import com.example.do_an_tot_nghiep.Notificationpage.NotificationViewModel;
import com.example.do_an_tot_nghiep.Repository.AppointmentQueueRepository;
import com.example.do_an_tot_nghiep.Repository.AppointmentRepository;
import com.example.do_an_tot_nghiep.Repository.BookingRepository;
import com.example.do_an_tot_nghiep.Repository.BookingPhotoRepository;
import com.example.do_an_tot_nghiep.Repository.DoctorRepository;
import com.example.do_an_tot_nghiep.Repository.NotificationRepository;
import com.example.do_an_tot_nghiep.Repository.ServiceRepository;
import com.example.do_an_tot_nghiep.Repository.SpecialityRepository;
import com.example.do_an_tot_nghiep.Repository.SynchronousTaskExecutorRule;
import com.example.do_an_tot_nghiep.Repository.TreatmentRepository;
import com.example.do_an_tot_nghiep.Repository.RecordRepository;

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

    // Test Case ID: TC-VM-013
    // DoctorpageViewModel - null repository crash
    @Test
    public void givenFreshDoctorpageViewModelWhenReadByIdCalledThenShouldNotCrash() {
        DoctorpageViewModel viewModelUnderTest = new DoctorpageViewModel();

        // This fails because repository is null unless instantiate() is called first.
        viewModelUnderTest.readById(headers, "D1");
    }

    // Test Case ID: TC-VM-014
    // DoctorpageViewModel - repository binding success
    @Test
    public void givenInjectedDoctorRepositoryWhenReadByIdSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        DoctorpageViewModel viewModelUnderTest = new DoctorpageViewModel();
        SingleLiveEvent<DoctorReadByID> repositoryLiveData = new SingleLiveEvent<>();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();

        doReturn(repositoryAnimation).when(doctorRepository).getAnimation();
        doReturn(repositoryLiveData).when(doctorRepository).getReadByIdResponse();
        setPrivateField(viewModelUnderTest, "repository", doctorRepository);

        viewModelUnderTest.readById(headers, "D1");
        AtomicReference<DoctorReadByID> observedValue = observe(viewModelUnderTest.getResponse());
        verify(doctorRepository).readById(headers, "D1");
        repositoryLiveData.setValue(mock(DoctorReadByID.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-015
    // SearchpageViewModel - null repository crash for doctorReadAll
    @Test
    public void givenFreshSearchpageViewModelWhenDoctorReadAllCalledThenShouldNotCrash() {
        SearchpageViewModel viewModelUnderTest = new SearchpageViewModel();

        // This fails because doctorRepository is null unless instantiate() is called first.
        viewModelUnderTest.doctorReadAll(headers, parameters);
    }

    // Test Case ID: TC-VM-016
    // SearchpageViewModel - repository binding success
    @Test
    public void givenInjectedSearchpageRepositoriesWhenDoctorReadAllSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        SearchpageViewModel viewModelUnderTest = new SearchpageViewModel();
        SingleLiveEvent<DoctorReadAll> doctorLiveData = new SingleLiveEvent<>();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();

        doReturn(repositoryAnimation).when(doctorRepository).getAnimation();
        doReturn(doctorLiveData).when(doctorRepository).getReadAllResponse();
        setPrivateField(viewModelUnderTest, "doctorRepository", doctorRepository);

        viewModelUnderTest.doctorReadAll(headers, parameters);
        AtomicReference<DoctorReadAll> observedDoctor = observe(viewModelUnderTest.getDoctorReadAllResponse());
        verify(doctorRepository).readAll(headers, parameters);
        doctorLiveData.setValue(mock(DoctorReadAll.class));

        assertSame(doctorLiveData, viewModelUnderTest.getDoctorReadAllResponse());
        assertNotNull(observedDoctor.get());
    }

    // Test Case ID: TC-VM-017
    // SearchpageViewModel - null repository crash for specialityReadAll
    @Test
    public void givenFreshSearchpageViewModelWhenSpecialityReadAllCalledThenShouldNotCrash() {
        SearchpageViewModel viewModelUnderTest = new SearchpageViewModel();

        // This fails because specialityRepository is null unless instantiate() is called first.
        viewModelUnderTest.specialityReadAll(headers, parameters);
    }

    // Test Case ID: TC-VM-018
    // SearchpageViewModel - repository binding success for speciality
    @Test
    public void givenInjectedSearchpageRepositoriesWhenSpecialityReadAllSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        SearchpageViewModel viewModelUnderTest = new SearchpageViewModel();
        SingleLiveEvent<com.example.do_an_tot_nghiep.Container.SpecialityReadAll> specialityLiveData = new SingleLiveEvent<>();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();

        doReturn(repositoryAnimation).when(specialityRepository).getAnimation();
        doReturn(specialityLiveData).when(specialityRepository).getReadAllResponse();
        setPrivateField(viewModelUnderTest, "specialityRepository", specialityRepository);

        viewModelUnderTest.specialityReadAll(headers, parameters);
        AtomicReference<com.example.do_an_tot_nghiep.Container.SpecialityReadAll> observedSpeciality = observe(viewModelUnderTest.getSpecialityReadAll());
        verify(specialityRepository).readAll(headers, parameters);
        specialityLiveData.setValue(mock(com.example.do_an_tot_nghiep.Container.SpecialityReadAll.class));

        assertSame(specialityLiveData, viewModelUnderTest.getSpecialityReadAll());
        assertNotNull(observedSpeciality.get());
    }

    // Test Case ID: TC-VM-019
    // SearchpageViewModel - null repository crash for serviceReadAll
    @Test
    public void givenFreshSearchpageViewModelWhenServiceReadAllCalledThenShouldNotCrash() {
        SearchpageViewModel viewModelUnderTest = new SearchpageViewModel();

        // This fails because serviceRepository is null unless instantiate() is called first.
        viewModelUnderTest.serviceReadAll(headers, parameters);
    }

    // Test Case ID: TC-VM-020
    // SearchpageViewModel - repository binding success for service
    @Test
    public void givenInjectedSearchpageRepositoriesWhenServiceReadAllSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        SearchpageViewModel viewModelUnderTest = new SearchpageViewModel();
        SingleLiveEvent<ServiceReadAll> serviceLiveData = new SingleLiveEvent<>();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();

        doReturn(repositoryAnimation).when(serviceRepository).getAnimation();
        doReturn(serviceLiveData).when(serviceRepository).getReadAllResponse();
        setPrivateField(viewModelUnderTest, "serviceRepository", serviceRepository);

        viewModelUnderTest.serviceReadAll(headers, parameters);
        AtomicReference<ServiceReadAll> observedService = observe(viewModelUnderTest.getServiceReadAllResponse());
        verify(serviceRepository).readAll(headers, parameters);
        serviceLiveData.setValue(mock(ServiceReadAll.class));

        assertSame(serviceLiveData, viewModelUnderTest.getServiceReadAllResponse());
        assertNotNull(observedService.get());
    }

    // Test Case ID: TC-VM-021
    // ServicepageViewModel - null repository crash
    @Test
    public void givenFreshServicepageViewModelWhenReadByIdCalledThenShouldNotCrash() {
        ServicepageViewModel viewModelUnderTest = new ServicepageViewModel();

        // This fails because repository is null unless instantiate() is called first.
        viewModelUnderTest.readById(headers, "S1");
    }

    // Test Case ID: TC-VM-022
    // ServicepageViewModel - repository binding success
    @Test
    public void givenInjectedServicepageRepositoriesWhenReadByIdSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        ServicepageViewModel viewModelUnderTest = new ServicepageViewModel();
        SingleLiveEvent<ServiceReadByID> repositoryLiveData = new SingleLiveEvent<>();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();

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

    // Test Case ID: TC-VM-023
    // ServicepageViewModel - null doctorRepository crash for doctorReadAll
    @Test
    public void givenFreshServicepageViewModelWhenDoctorReadAllCalledThenShouldNotCrash() {
        ServicepageViewModel viewModelUnderTest = new ServicepageViewModel();

        // This fails because doctorRepository is null unless instantiate() is called first.
        viewModelUnderTest.doctorReadAll(headers, parameters);
    }

    // Test Case ID: TC-VM-024
    // ServicepageViewModel - repository binding success for doctorReadAll
    @Test
    public void givenInjectedServicepageRepositoriesWhenDoctorReadAllSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        ServicepageViewModel viewModelUnderTest = new ServicepageViewModel();
        SingleLiveEvent<DoctorReadAll> doctorLiveData = new SingleLiveEvent<>();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();

        doReturn(repositoryAnimation).when(doctorRepository).getAnimation();
        doReturn(doctorLiveData).when(doctorRepository).getReadAllResponse();
        setPrivateField(viewModelUnderTest, "doctorRepository", doctorRepository);

        viewModelUnderTest.doctorReadAll(headers, parameters);
        AtomicReference<DoctorReadAll> observedDoctor = observe(viewModelUnderTest.getDoctorReadAllResponse());
        verify(doctorRepository).readAll(headers, parameters);
        doctorLiveData.setValue(mock(DoctorReadAll.class));

        assertSame(doctorLiveData, viewModelUnderTest.getDoctorReadAllResponse());
        assertNotNull(observedDoctor.get());
    }

    // Test Case ID: TC-VM-025
    // SpecialitypageViewModel - null repository crash
    @Test
    public void givenFreshSpecialitypageViewModelWhenReadByIdCalledThenShouldNotCrash() {
        SpecialitypageViewModel viewModelUnderTest = new SpecialitypageViewModel();

        // This fails because repository is null unless instantiate() is called first.
        viewModelUnderTest.readById(headers, "SP1");
    }

    // Test Case ID: TC-VM-026
    // SpecialitypageViewModel - repository binding success
    @Test
    public void givenInjectedSpecialitypageRepositoriesWhenReadByIdSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        SpecialitypageViewModel viewModelUnderTest = new SpecialitypageViewModel();
        SingleLiveEvent<SpecialityReadByID> repositoryLiveData = new SingleLiveEvent<>();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();

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

    // Test Case ID: TC-VM-027
    // SpecialitypageViewModel - null doctorRepository crash
    @Test
    public void givenFreshSpecialitypageViewModelWhenDoctorReadAllCalledThenShouldNotCrash() {
        SpecialitypageViewModel viewModelUnderTest = new SpecialitypageViewModel();

        // This fails because doctorRepository is null unless instantiate() is called first.
        viewModelUnderTest.doctorReadAll(headers, parameters);
    }

    // Test Case ID: TC-VM-028
    // SpecialitypageViewModel - repository binding success for doctorReadAll
    @Test
    public void givenInjectedSpecialitypageRepositoriesWhenDoctorReadAllSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        SpecialitypageViewModel viewModelUnderTest = new SpecialitypageViewModel();
        SingleLiveEvent<DoctorReadAll> doctorLiveData = new SingleLiveEvent<>();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();

        doReturn(repositoryAnimation).when(doctorRepository).getAnimation();
        doReturn(doctorLiveData).when(doctorRepository).getReadAllResponse();
        setPrivateField(viewModelUnderTest, "doctorRepository", doctorRepository);

        viewModelUnderTest.doctorReadAll(headers, parameters);
        AtomicReference<DoctorReadAll> observedDoctor = observe(viewModelUnderTest.getDoctorReadAllResponse());
        verify(doctorRepository).readAll(headers, parameters);
        doctorLiveData.setValue(mock(DoctorReadAll.class));

        assertSame(doctorLiveData, viewModelUnderTest.getDoctorReadAllResponse());
        assertNotNull(observedDoctor.get());
    }

    // Test Case ID: TC-VM-029
    // RecordpageViewModel - null repository crash
    @Test
    public void givenFreshRecordpageViewModelWhenReadByIdCalledThenShouldNotCrash() {
        RecordpageViewModel viewModelUnderTest = new RecordpageViewModel();

        // This fails because repository is null unless instantiate() is called first.
        viewModelUnderTest.readByID(headers, "R1");
    }

    // Test Case ID: TC-VM-030
    // RecordpageViewModel - repository binding success
    @Test
    public void givenInjectedRecordpageRepositoryWhenReadByIdSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        RecordpageViewModel viewModelUnderTest = new RecordpageViewModel();
        MutableLiveData<RecordReadByID> repositoryLiveData = new MutableLiveData<>();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();

        doReturn(repositoryAnimation).when(recordRepository).getAnimation();
        doReturn(repositoryLiveData).when(recordRepository).readByID(any(), any());
        setPrivateField(viewModelUnderTest, "repository", recordRepository);

        viewModelUnderTest.readByID(headers, "R1");

        // RecordRepository.readByID returns MutableLiveData directly
        assertNotNull(viewModelUnderTest.getReadByIDResponse());
    }

    // Test Case ID: TC-VM-031
    // SettingspageViewModel - null appointmentRepository crash
    @Test
    public void givenFreshSettingspageViewModelWhenReadAllCalledThenShouldNotCrash() {
        SettingspageViewModel viewModelUnderTest = new SettingspageViewModel();

        // This fails because appointmentRepository is null unless instantiate() is called first.
        viewModelUnderTest.readAll(headers, parameters);
    }

    // Test Case ID: TC-VM-032
    // SettingspageViewModel - repository binding success
    @Test
    public void givenInjectedSettingspageRepositoriesWhenReadAllSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        SettingspageViewModel viewModelUnderTest = new SettingspageViewModel();
        SingleLiveEvent<AppointmentReadAll> repositoryLiveData = new SingleLiveEvent<>();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();

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

    // Test Case ID: TC-VM-033
    // SettingspageViewModel - null bookingRepository crash for bookingReadAll
    @Test
    public void givenFreshSettingspageViewModelWhenBookingReadAllCalledThenShouldNotCrash() {
        SettingspageViewModel viewModelUnderTest = new SettingspageViewModel();

        // This fails because bookingRepository is null unless instantiate() is called first.
        viewModelUnderTest.bookingReadAll(headers, parameters);
    }

    // Test Case ID: TC-VM-034
    // SettingspageViewModel - repository binding success for bookingReadAll
    @Test
    public void givenInjectedSettingspageRepositoriesWhenBookingReadAllSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        SettingspageViewModel viewModelUnderTest = new SettingspageViewModel();
        SingleLiveEvent<BookingReadAll> repositoryLiveData = new SingleLiveEvent<>();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();

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

    // Test Case ID: TC-VM-035
    // TreatmentpageViewModel - null treatmentRepository crash
    @Test
    public void givenFreshTreatmentpageViewModelWhenTreatmentReadAllCalledThenShouldNotCrash() {
        TreatmentpageViewModel viewModelUnderTest = new TreatmentpageViewModel();

        // This fails because treatmentRepository is null unless instantiate() is called first.
        viewModelUnderTest.treatmentReadAll(headers, "A1");
    }

    // Test Case ID: TC-VM-036
    // TreatmentpageViewModel - repository binding success
    @Test
    public void givenInjectedTreatmentpageRepositoriesWhenTreatmentReadAllSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        TreatmentpageViewModel viewModelUnderTest = new TreatmentpageViewModel();
        SingleLiveEvent<TreatmentReadAll> repositoryLiveData = new SingleLiveEvent<>();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();

        doReturn(repositoryAnimation).when(treatmentRepository).getAnimation();
        doReturn(repositoryLiveData).when(treatmentRepository).getReadAllResponse();
        setPrivateField(viewModelUnderTest, "treatmentRepository", treatmentRepository);

        viewModelUnderTest.treatmentReadAll(headers, "A1");
        AtomicReference<TreatmentReadAll> observedValue = observe(viewModelUnderTest.getTreatmentReadAllResponse());
        repositoryLiveData.setValue(mock(TreatmentReadAll.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getTreatmentReadAllResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-037
    // TreatmentpageViewModel - null appointmentRepository crash for appointmentReadAll
    @Test
    public void givenFreshTreatmentpageViewModelWhenAppointmentReadAllCalledThenShouldNotCrash() {
        TreatmentpageViewModel viewModelUnderTest = new TreatmentpageViewModel();

        // This fails because appointmentRepository is null unless instantiate() is called first.
        viewModelUnderTest.appointmentReadAll(headers, parameters);
    }

    // Test Case ID: TC-VM-038
    // TreatmentpageViewModel - repository binding success for appointmentReadAll
    @Test
    public void givenInjectedTreatmentpageRepositoriesWhenAppointmentReadAllSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        TreatmentpageViewModel viewModelUnderTest = new TreatmentpageViewModel();
        SingleLiveEvent<AppointmentReadAll> repositoryLiveData = new SingleLiveEvent<>();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();

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

    // Test Case ID: TC-VM-039
    // TreatmentpageViewModel - null treatmentRepository crash for treatmentReadByID
    @Test
    public void givenFreshTreatmentpageViewModelWhenTreatmentReadByIdCalledThenShouldNotCrash() {
        TreatmentpageViewModel viewModelUnderTest = new TreatmentpageViewModel();

        // This fails because treatmentRepository is null unless instantiate() is called first.
        viewModelUnderTest.treatmentReadByID(headers, "T1");
    }

    // Test Case ID: TC-VM-040
    // BookingpageViewModel - null bookingRepository crash for bookingReadByID
    @Test
    public void givenFreshBookingpageViewModelWhenBookingReadByIdCalledThenShouldNotCrash() {
        BookingpageViewModel viewModelUnderTest = new BookingpageViewModel();

        // This fails because bookingRepository is null unless instantiate() is called first.
        viewModelUnderTest.bookingReadByID(headers, "B1");
    }

    // Test Case ID: TC-VM-041
    // BookingpageViewModel - repository binding success for bookingReadByID
    @Test
    public void givenInjectedBookingpageRepositoriesWhenBookingReadByIdSuccessThenViewModelExposesRepositoryLiveData() throws Exception {
        BookingpageViewModel viewModelUnderTest = new BookingpageViewModel();
        SingleLiveEvent<BookingReadByID> repositoryLiveData = new SingleLiveEvent<>();
        MutableLiveData<Boolean> repositoryAnimation = new MutableLiveData<>();

        doReturn(repositoryAnimation).when(bookingRepository).getAnimation();
        doReturn(repositoryLiveData).when(bookingRepository).getReadByIDResponse();
        setPrivateField(viewModelUnderTest, "bookingRepository", bookingRepository);

        viewModelUnderTest.bookingReadByID(headers, "B1");
        AtomicReference<BookingReadByID> observedValue = observe(viewModelUnderTest.getBookingReadByIdResponse());
        verify(bookingRepository).readByID(headers, "B1");
        repositoryLiveData.setValue(mock(BookingReadByID.class));

        assertSame(repositoryLiveData, viewModelUnderTest.getBookingReadByIdResponse());
        assertNotNull(observedValue.get());
    }

    // Test Case ID: TC-VM-042
    // BookingpageViewModel - null bookingPhotoRepository crash
    @Test
    public void givenFreshBookingpageViewModelWhenBookingPhotoReadAllCalledThenShouldNotCrash() {
        BookingpageViewModel viewModelUnderTest = new BookingpageViewModel();

        // This fails because bookingPhotoRepository is null unless instantiate() is called first.
        viewModelUnderTest.bookingPhotoReadAll(headers, "B1");
    }

    // Test Case ID: TC-VM-043
    // BookingpageViewModel - null doctorRepository crash for doctorReadByID
    @Test
    public void givenFreshBookingpageViewModelWhenDoctorReadByIdCalledThenShouldNotCrash() {
        BookingpageViewModel viewModelUnderTest = new BookingpageViewModel();

        // This fails because doctorRepository is null unless instantiate() is called first.
        viewModelUnderTest.doctorReadByID(headers, "D1");
    }

    // Test Case ID: TC-VM-044
    // AppointmentpageViewModel - null repository crash for readByID
    @Test
    public void givenFreshAppointmentpageViewModelWhenReadByIdCalledThenShouldNotCrash() {
        AppointmentpageViewModel viewModelUnderTest = new AppointmentpageViewModel();

        // This fails because repository is null unless instantiate() is called first.
        viewModelUnderTest.readByID(headers, "A1");
    }

    // Test Case ID: TC-VM-045
    // AppointmentpageViewModel - null queueRepository crash for getQueue
    @Test
    public void givenFreshAppointmentpageViewModelWhenGetQueueCalledThenShouldNotCrash() {
        AppointmentpageViewModel viewModelUnderTest = new AppointmentpageViewModel();

        // This fails because queueRepository is null unless instantiate() is called first.
        viewModelUnderTest.getQueue(headers, parameters);
    }

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



