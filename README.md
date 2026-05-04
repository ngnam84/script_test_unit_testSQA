# Script Test Unit Test - SQA

Repository chứa các script test và unit test cho dự án Android.

## Cấu trúc thư mục

```
do_an_tot_nghiep/
├── Container/
│   └── ContainerModelsTest.java
├── Model/
│   └── ModelEntityLogicTest.java
├── Repository/
│   ├── RepositoryTest.java
│   ├── RepositoryBugDetectionTest.java
│   └── SynchronousTaskExecutorRule.java
├── HelperTest.java
├── MainViewModelTest.java
├── ViewModelBugDetectionTest.java
└── ExampleUnitTest.java
```

## Mô tả các file test

### Container Models Test
- Test cho các Container Models trong ứng dụng

### Model Entity Logic Test
- Test logic của các Entity Models

### Repository Tests
- `RepositoryTest.java` - Unit tests cho Repository layer
- `RepositoryBugDetectionTest.java` - Test phát hiện bugs trong Repository
- `SynchronousTaskExecutorRule.java` - Rule hỗ trợ test async

### ViewModel Tests
- `MainViewModelTest.java` - Test cho MainViewModel
- `ViewModelBugDetectionTest.java` - Test phát hiện bugs trong ViewModel

### Helper Tests
- `HelperTest.java` - Test cho các Helper classes

### Other Tests
- `ExampleUnitTest.java` - Test mẫu

## Yêu cầu
- Java 8+
- Android SDK
- JUnit 4
- Mockito

## Chạy test
```bash
./gradlew test
```

## Tác giả
- Nguyễn Nam
