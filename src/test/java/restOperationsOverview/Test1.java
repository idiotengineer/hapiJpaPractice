package restOperationsOverview;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.Application;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class Test1 {


	@Test
	@DisplayName("vread 메서드는 @Read 어노테이션에 (version = true) 속성을 붙이면 된다.")
	@Read(version = false)
	public void vreadMethod() {
		Patient postedPatient = (Patient) postPatient().getResource();
		FhirContext ctx = FhirContext.forR4();
		IGenericClient client = ctx.newRestfulGenericClient("http://hapi.fhir.org/baseR4");

		Patient execute = client
			.read()
			.resource(Patient.class)
			.withId(postedPatient.getIdPart())
			.execute();

		IParser parser = ctx.newJsonParser();

		String encoded = parser.setPrettyPrint(true)
			.encodeResourceToString(execute);

		System.out.println(encoded);
	}

	@Create
	public Patient postPatientWithVersion(String resourceId, String versionId) {
		Patient patient = new Patient();

		patient.setId(resourceId);
		patient.getMeta().setVersionId(versionId);
		patient.addName().setFamily("천").addGiven("혁성");

		// 클라이언트 & 컨텍스트를 생성해서 통신 시작
		FhirContext ctx = FhirContext.forR4();
		IGenericClient client = ctx.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
		client
			.create()
			.resource(patient)
			.execute();

		return patient;
	}

	@Create
	public MethodOutcome postPatient() {
		Patient patient = new Patient();

		patient.addName().setFamily("천").addGiven("asdasd1231235");
		patient.setGender(Enumerations.AdministrativeGender.MALE);

		// 클라이언트 & 컨텍스트를 생성해서 통신 시작
		FhirContext ctx = FhirContext.forR4();
		IGenericClient client = ctx.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
		MethodOutcome execute = client
			.create()
			.resource(patient)
			.execute();


		return execute;
	}

	@Test
	@DisplayName("conditionalUrlParam을 이용하여 조건부 업데이트를 하는 메서드이다")
	public void conditionalUrlParamTest() {
		// 중복되는 조건으로 여러개의 리소스가 조회가 되면 에러를 발생시키기 때문에 계속 변경사항을 교체해줘야 테스트가 됨.

		// FhirContext 초기화
		FhirContext fhirContext = FhirContext.forR4();

		// FHIR 서버 URL 설정
		String serverBase = "http://hapi.fhir.org/baseR4";

		// FHIR 클라이언트 생성
		IGenericClient fhirClient = fhirContext.newRestfulGenericClient(serverBase);

		// Conditional Update를 위한 검색 조건 설정 (이름이 "John"인 환자)
		String condition = "name=asdasd1231235";

		// Conditional Update를 위해 @Update 어노테이션 사용
		Patient patient = (Patient) postPatient().getResource();

		List<HumanName> names = new ArrayList<>();
		names.add(new HumanName().addGiven("장철").setFamily("천"));
		patient.setName(names);

		// Conditional Update 요청
		Patient updatedPatient = (Patient) fhirClient.update().resource(patient)
			.conditionalByUrl("Patient?" + condition) // 조건을 URL 파라미터로 설정
			.execute().getResource();

		// 업데이트된 환자의 ID 출력
		System.out.println("Updated Patient ID: " + updatedPatient.getName().get(0).getGivenAsSingleString());
	}


	@Test
	@DisplayName("데이터 유효성 검사를 실행하는 메서드")
	public void resourceValidateTest() {
		// 어노테이션으로 파라미터에도 적용이 가능하다. 그럼 코드에 굳이 이렇게 유효성 검사 코드를 적지 않아도 된다.
		FhirContext ctx = FhirContext.forR4();
		String serverURL = "http://hapi.fhir.org/baseR4";

		IGenericClient client = ctx.newRestfulGenericClient(serverURL);

		Patient patient = (Patient) postPatient().getResource();

		MethodOutcome execute = client.validate().resource(patient).execute();

		OperationOutcome operationOutcome =(OperationOutcome) execute.getOperationOutcome();
		if (operationOutcome.getIssue().isEmpty()) {
			System.out.println("유효성 검사 통과");
		} else {
			System.out.println("유효성 검사 실패");
			operationOutcome.getIssue().forEach(issue -> System.out.println(issue.getDetails().getText()));
		}
	}

	@Test
	@DisplayName("번들을 이용하는 CRUD 작업이다. 여러개의 FHIR 리소스를 단일 요청에 포함할 수 있는 방법이다.")
	public void transactionCRUDTest() {
		// 번들을 이용하면 여러 리소스를 단일 요청에 포함할 수 있어, 효율적이다.
		// 반면 CRUD를 FHIR 어노테이션을 사용하면 서버 구현 측면에서 더 직관적이고 편리하다.

		String serverURL = "http://hapi.fhir.org/baseR4";
		FhirContext ctx = FhirContext.forR4();
		IGenericClient client = ctx.newRestfulGenericClient(serverURL);

		Patient patient = new Patient();
		patient.addName().addGiven("KiM").setFamily("Chan ho");
		patient.setGender(Enumerations.AdministrativeGender.MALE);

		//Create
		MethodOutcome created = client.create().resource(patient).execute();
		String idPart = ((Patient) created.getResource()).getIdPart();
		System.out.println("Created Patient ID : " + idPart);

		//Read
		Patient read = client.read().resource(Patient.class).withId(idPart).execute();
		System.out.println("Read patient Id : " + read.getIdPart());

		//Update
		read.setActive(true);
		MethodOutcome updated = client.update().resource(read).execute();
		System.out.println("Update Id : " + ((Patient) updated.getResource()).getIdPart());

		//Delete
		MethodOutcome delete = client.delete().resource(patient).execute();
		System.out.println("delete status : " + delete.getResponseStatusCode());
	}
}
