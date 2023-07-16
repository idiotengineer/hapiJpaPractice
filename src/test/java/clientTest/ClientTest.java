package clientTest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.Application;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ClientTest {

	@Test
	@DisplayName("간단한 client 코드 hapi-fhir 서버에서 회원 조회")
	public void genericClientGetTest() {
		FhirContext ctx = FhirContext.forR4();
		String serverURL = "http://hapi.fhir.org/baseR4";

		IGenericClient client = ctx.newRestfulGenericClient(serverURL);

		Bundle execute = client
			.search()
			.forResource(Patient.class)
			.where(Patient.FAMILY.matches().value("John"))
			.returnBundle(Bundle.class)
			.execute();

		if (execute.hasEntry()) {
			Patient resource = (Patient) execute.getEntry().get(0).getResource();

			if (resource.hasName()) {
				System.out.println(resource.getName().get(0).getFamily() + resource.getName().get(0).getGivenAsSingleString());
			}
		}
	}

	@Test
	@DisplayName("간단한 client 코드 hapi-fhir 서버에서 회원 생성")
	public void genericClientPostTest() {
		FhirContext ctx = FhirContext.forR4();
		String serverURL = "http://hapi.fhir.org/baseR4";
		IGenericClient client = ctx.newRestfulGenericClient(serverURL);

		Patient patient = new Patient();
		patient.addName().setFamily("천").addGiven("혁성");
		patient.addIdentifier().setSystem("urn:example:system").setValue("12345");

		MethodOutcome execute = client.create().resource(patient).execute();

		if (execute.getResource() instanceof Patient) {
			Patient resource = (Patient) execute.getResource();

			System.out.println(resource.getId());
			System.out.println(resource.getName().get(0).getFamily());
			System.out.println(resource.getName().get(0).getGivenAsSingleString());
		}
	}

	@Test
	public void genericClientDeleteTest() {
		FhirContext ctx = FhirContext.forR4();
		String serverURL = "http://hapi.fhir.org/baseR4";

		Patient patient = new Patient();
		patient.addName().setFamily("천").addGiven("혁성");
		patient.addIdentifier().setSystem("urn:example:system").setValue("12345");

		//POST
		IGenericClient client = ctx.newRestfulGenericClient(serverURL);
		MethodOutcome execute = client.create().resource(patient).execute();

		//GET
		Patient postedPatient = client.read().resource(Patient.class).withId(((Patient) execute.getResource()).getId()).execute();

		if (postedPatient != null) {
			System.out.println("POST 된 patient 조회");

			System.out.println(postedPatient.getId());
			System.out.println(postedPatient.getName().get(0).getFamily());
			System.out.println(postedPatient.getName().get(0).getGivenAsSingleString());
		}

		//DELETE
		MethodOutcome deleted = client.delete().resourceById(new IdType("Patient", postedPatient.getIdPart())).execute();
		if (deleted.getResponseStatusCode() >= 200 && deleted.getResponseStatusCode() <= 300) {
			System.out.println("OK");
		}
	}
}
