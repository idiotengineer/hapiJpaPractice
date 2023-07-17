package restOperationsSearch;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.Application;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.DateClientParam;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SpringBootTest(classes = Application.class)
@RunWith(SpringRunner.class)
public class Test1 {

	@Test
	@DisplayName("매개변수 없이 조회 (실행하지 말자 모든 환자를 조회하게 해놓음)")
	public void searchWithNoParam() {
		String serverURL = "http://hapi.fhir.org/baseR4";
		FhirContext ctx = FhirContext.forR4();
		IGenericClient client = ctx.newRestfulGenericClient(serverURL);

		List<Bundle.BundleEntryComponent> entry = client
			.search()
			.forResource(Patient.class)
			.returnBundle(Bundle.class)
			.execute()
			.getEntry();
	}

	@Test
	@DisplayName("매개변수를 사용해보기1")
	public void searchWithParam() {
		// 마찬가지로  @RequiredParam 어노테이션을 사용해서 파라미터에 적용가능하다.
		FhirContext fhirContext = FhirContext.forR4();
		String serverBase = "http://hapi.fhir.org/baseR4";
		IGenericClient fhirClient = fhirContext.newRestfulGenericClient(serverBase);

		// Patient 리소스 생성
		Patient patient = new Patient();
		patient.addName().setFamily("Smith").addGiven("John");
		patient.addTelecom().setSystem(org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem.PHONE)
			.setValue("123-456-7890");

		// @Create 어노테이션을 이용하여 FHIR 서버에 리소스 생성 요청
		MethodOutcome createdPatientOutcome = fhirClient.create().resource(patient).execute();

		// 생성된 Patient의 ID 가져오기
		IIdType createdPatientId = createdPatientOutcome.getId();
		System.out.println("Created Patient ID: " + createdPatientId.getValue());

		// 생성된 Patient 리소스를 @RequiredParam을 이용하여 다양한 방법으로 조회
		// 방법 1: ID로 조회
		Patient fetchedPatientById = fhirClient.read().resource(Patient.class).withId(createdPatientId).execute();
		System.out.println("Fetched Patient by ID:");
		System.out.println(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(fetchedPatientById));

		// 방법 2: Family Name으로 조회
		IBaseBundle searchResultsByFamilyName = fhirClient.search()
			.forResource(Patient.class)
			.where(Patient.FAMILY.matches().value("Smith"))
			.returnBundle(org.hl7.fhir.r4.model.Bundle.class)
			.execute();

		System.out.println("Search Results by Family Name:");
		System.out.println(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(searchResultsByFamilyName));
	}

	@Test
	@DisplayName("매개변수를 사용해보기2")
	public void searchWithParam2(){
		// FhirContext 초기화
		FhirContext fhirContext = FhirContext.forR4();
		String serverBase = "http://hapi.fhir.org/baseR4";
		IGenericClient fhirClient = fhirContext.newRestfulGenericClient(serverBase);

		// Patient 리소스 생성
		Patient patient = new Patient();
		patient.addName().setFamily("Smith").addGiven("John");
		patient.addTelecom().setSystem(org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem.PHONE)
			.setValue("123-456-7890");

		// @Create 어노테이션을 이용하여 FHIR 서버에 리소스 생성 요청
		IIdType createdPatientId = fhirClient.create().resource(patient).execute().getId();
		System.out.println("Created Patient ID: " + createdPatientId.getValue());

		// 생성된 Patient 리소스를 @RequiredParam을 이용하여 다양한 방법으로 조회
		// 방법 1: ID로 조회
		Patient fetchedPatientById = fhirClient.read().resource(Patient.class).withId(createdPatientId).execute();
		System.out.println("Fetched Patient by ID:");
		System.out.println(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(fetchedPatientById));

		// 방법 2: Family Name으로 조회
		Bundle searchResultsByFamilyName = fhirClient.search()
			.forResource(Patient.class)
			.where(Patient.FAMILY.matches().value("Smith"))
			.returnBundle(Bundle.class)
			.execute();
		System.out.println("Search Results by Family Name:");
		System.out.println(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(searchResultsByFamilyName));

		// 방법 3: 여러 조건을 사용하여 조회 (Family Name과 Given Name이 모두 "Smith John"인 경우)
		IBaseBundle searchResultsByMultipleConditions = fhirClient.search()
			.forResource(Patient.class)
			.where(new StringClientParam("family").matches().value("Smith"))
			.where(new StringClientParam("given").matches().value("John"))
			.returnBundle(Bundle.class)
			.execute();
		System.out.println("Search Results by Multiple Conditions (Family Name and Given Name):");
		System.out.println(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(searchResultsByMultipleConditions));

		// 방법 4: 페이징을 사용하여 조회 (10개의 결과를 한 페이지로 조회)
		Bundle pagedSearchResults = fhirClient.search()
			.forResource(Patient.class)
			.count(10)
			.returnBundle(Bundle.class)
			.execute();
		System.out.println("Paged Search Results (Page 1):");
		System.out.println(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(pagedSearchResults));

		// 다음 페이지를 가져오려면 Bundle의 link에 있는 "next" 링크를 사용하여 쿼리를 실행하면 됩니다.
	}

	@Test
	@DisplayName("매개변수를 사용해보기3")
	public void searchWithParam3() {
		FhirContext fhirContext = FhirContext.forR4();
		String serverBase = "http://hapi.fhir.org/fhir";
		IGenericClient fhirClient = fhirContext.newRestfulGenericClient(serverBase);

		// Patient 리소스 생성
		Patient patient = new Patient();
		patient.addName().setFamily("Smith").addGiven("John");
		patient.addTelecom().setSystem(org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem.PHONE)
			.setValue("123-456-7890");

		// @Create 어노테이션을 이용하여 FHIR 서버에 리소스 생성 요청
		IIdType createdPatientId = fhirClient.create().resource(patient).execute().getId();
		System.out.println("Created Patient ID: " + createdPatientId.getValue());


		// 방법 5: 정렬하여 조회 (생일 기준으로 오름차순 정렬)
		IBaseBundle sortedSearchResults = fhirClient.search()
			.forResource(Patient.class)
			.sort().ascending(Patient.SP_BIRTHDATE)
			.returnBundle(Bundle.class)
			.execute();
		System.out.println("Sorted Search Results (Created Date Ascending):");
		System.out.println(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(sortedSearchResults));
	}

	
	@Test
	@DisplayName("메서드 체이닝 테스트")
	public void methodChainingTest() {
		// 메서드를 체이닝 할 수 있다. 아래는 환자 리소스를 조회하면서 관련된 일반 의사 정보를 Include 시켜서 같이 불러온다.
		String serverBase = "http://hapi.fhir.org/baseR4";
		FhirContext ctx = FhirContext.forR4();
		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// Chained Resource References (환자 리소스와 관련된 리소스 검색)
		List<Bundle.BundleEntryComponent> entry = client
			.search()
			.forResource(Patient.class)
			.where(Patient.NAME.matches().value("John")) // 환자 이름이 "John"인 환자를 검색
			.include(Patient.INCLUDE_GENERAL_PRACTITIONER) // 환자와 관련된 일반 의사 정보를 검색 (include 지정)
			.returnBundle(Bundle.class)
			.execute()
			.getEntry();


		for (Bundle.BundleEntryComponent bundleEntryComponent : entry) {
			Patient patient = (Patient) bundleEntryComponent.getResource();
			System.out.println("Patient ID: " + patient.getIdElement().getIdPart());
			System.out.println("Patient Name: " + patient.getNameFirstRep().getNameAsSingleString());
			System.out.println("------------------------------------");

			// 관련된 리소스 정보 출력 (여기서는 일반 의사 정보만을 출력)
			if (patient.hasGeneralPractitioner()) {
				System.out.println("General Practitioner Reference: " + patient.getGeneralPractitioner().get(0).getReference());
				System.out.println("------------------------------------");
			}
		}
	}
}
