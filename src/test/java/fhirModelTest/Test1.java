package fhirModelTest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.Application;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.assertj.core.api.Assertions;
import org.hl7.fhir.r4.model.*;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class Test1 {

	@Test
	public void testcode() {
		System.out.println("VVIP");
	}

	@Test
	@DisplayName("Deserializing(Parsing) Test")
	public void DeserializingTest() {
		FhirContext ctx = FhirContext.forR4();

		String input = "{" + "\"resourceType\" : \"Patient\","
			+ "  \"name\" : [{"
			+ "    \"family\": \"Simpson\""
			+ "  }]"
			+ "}";
		IParser parser = ctx.newJsonParser();

		Patient parsed = parser.parseResource(Patient.class, input);

		Assertions.assertThat(parsed.getName().get(0).getFamily()).isEqualTo("Simpson");
		System.out.println(parsed.getName().get(0).getFamily());
	}

	@Test
	@DisplayName(("encoding And Decoding With Json"))
	public void encodingAndDecodingTest() {
		FhirContext ctx = FhirContext.forR4();

		Patient patient = new Patient();
		patient.addName().setFamily("Simpson").addGiven("Hommer");

		IParser parser = ctx.newJsonParser();

		String encodedJSONString = parser.encodeResourceToString(patient);
		System.out.println(encodedJSONString);

		Patient decodedPateintInstance = parser.parseResource(Patient.class, encodedJSONString);
		System.out.println(decodedPateintInstance.getName().get(0).getFamily());
		System.out.println(decodedPateintInstance.getName().get(0).getGivenAsSingleString());
		// GivenName은 여러개가 올 수 있기 때문에 그냥 String으로 이어져서 보이게 getGiven 대신 getGivenAsSingleString 메서드 사용
	}

	@Test
	@DisplayName("get JSON Encoded String And get Easy to read Style")
	public void prettyPrintingTest() {
		Patient patient = new Patient();
		patient.addName().setFamily("Simpson").addGiven("Hommer");

		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();

		parser.setPrettyPrint(true);

		String encodedString = parser.encodeResourceToString(patient);
		System.out.println(encodedString);
	}

	@Test
	@DisplayName("여러가지 인코딩 설정 테스트")
	public void encodingConfigTest() {
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();

		Patient patient = new Patient();
		Identifier identifier = new Identifier();
		identifier.setSystem("system");
		identifier.setValue("value");
		identifier.setPeriod(new Period());

		List<Identifier> identifiers = new ArrayList<>();
		identifiers.add(identifier);
		patient.setIdentifier(identifiers);
		patient.setActive(true);
		patient.setGender(Enumerations.AdministrativeGender.MALE);
		patient.setLanguage("UTF-8");
		// 인코딩 시 해당 필드를 빼고 사용
		Set<String> strings = new HashSet<>();
		strings.add("Patient.identifier");
		strings.add("Patient.active");

		String encodedString = parser.setDontEncodeElements(
				strings
			).setPrettyPrint(true)
			.encodeResourceToString(patient);

		System.out.println(encodedString);

		//Narrative 설정 포함 (읽을 수 있는 설명 리소스의 내용을 나타내는데 사용함 Ex : HTML)
		String encodedString2 = parser.setSuppressNarratives(true)
			.setPrettyPrint(true)
			.encodeResourceToString(patient);

		System.out.println(encodedString2);
	}

	@Test
	@DisplayName("Resource Reference (리소스 참조)")
	public void resourceReferenceTest() {
		FhirContext ctx = FhirContext.forR4();
		Identifier identifier = new Identifier();
		identifier.setId("urn:mrns").setIdBase("253345");


		Patient patient = new Patient();
		patient.setId("Patient/1333");
		patient.addIdentifier(identifier);
		patient.getManagingOrganization().setReference("Organization/124362");
		// 리소스 내부에 다른 리소스에 대한 참조를 설정

		IParser parser = ctx.newJsonParser().setPrettyPrint(true);

		String encoded = parser.encodeResourceToString(patient);

		System.out.println(encoded);
	}


	@Test
	public void includeResourceTest() {
		// HAPI-FHIR 에서 그냥 리소스 조회 시 연관 리소스는 없다고 나오기 떄문에 아무 결과는 없다.
		FhirContext ctx = FhirContext.forR4();

		String serverURL = "http://hapi.fhir.org/baseR4";

		IGenericClient client = ctx.newRestfulGenericClient(serverURL);

		Bundle execute = client.search()
			.forResource(Patient.class)
			.where(Patient.RES_ID.exactly().code("12345"))
			.returnBundle(Bundle.class)
			.execute();

		if (execute.hasEntry()) {
			Patient patient = (Patient) execute.getEntryFirstRep().getResource();

			String reference = patient.getGeneralPractitionerFirstRep().getReference();

			System.out.println(reference);
		}
	}

	@Test
	@DisplayName("버전 정보 같이 출력")
	public void versionReference() {
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();

		Patient patient = new Patient();
		patient.setManagingOrganization(new Reference());

		Meta meta = new Meta();
		meta.setVersionId("1");
		patient.setMeta(meta); // 버전 설정

		String encoded = parser.setPrettyPrint(true).setStripVersionsFromReferences(false)
			.encodeResourceToString(patient);
		// 버전도 출력 정보에 포함시켜버린다.

		String encoded1 = parser.setDontStripVersionsFromReferencesAtPaths(
			"Patient"
		).encodeResourceToString(patient);
		// 해당 리소스들의 버전 정보는 스킵하지 않기로 하는 설정

		ctx.getParserOptions()
			.setDontStripVersionsFromReferencesAtPaths(
				"Patient"
			);

		System.out.println(encoded);
		System.out.println(encoded1);
	}

	@Test
	public void patientExtentsion() {
		FhirContext ctx = FhirContext.forR4();

		Patient patient = new Patient();
		patient.setId("123");
		patient.addIdentifier().setSystem("http://example.com").setValue("12345");

		Extension extension = new Extension();
		extension.setUrl("http://example.com/extension#myExtension");
		StringType value = new StringType("My Extension Value");
		extension.setValue(value);

		patient.addExtension(extension);

		IParser parser = ctx.newJsonParser();
		parser.setPrettyPrint(true);

		String encoded = parser.encodeResourceToString(patient);

		System.out.println(encoded);
	}


	@Test
	public void patientNarrativetest() {
		//Narrative는 html 코드를 생성하는 것으로 생각해보면 되는데 Thymeleaf랑 호환이 좋음
		Patient patient = new Patient();

		patient.addIdentifier().setSystem("urn:foo").setValue("7000135");
		patient.addName().setFamily("Smith").addGiven("John").addGiven("Edward");
		patient.addAddress()
			.addLine("742 Evergreen Terrace")
			.setCity("SpringField")
			.setState("ZZ");

		FhirContext ctx = FhirContext.forR4();

		ctx.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());

		String encoded = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);

		System.out.println(encoded);
	}

	@Test
	@DisplayName("나만의 Narrative 템플릿 테스트")
	public void narrativeTemplateCustomizeTest() throws IOException {
		FhirContext ctx = FhirContext.forR4();

		Patient patient = new Patient();
		patient.setId("123");
		patient.addIdentifier().setSystem("http://example.com").setValue("ABC1234");

		HumanName humanName = new HumanName();
		humanName.setFamily("천");
		humanName.addGiven("혁성");
		patient.addName(humanName);

		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
		resolver.setPrefix("customizedTemplate/");
		resolver.setSuffix(".html");
		TemplateEngine templateEngine = new TemplateEngine();
		templateEngine.setTemplateResolver(resolver);

		Context context = new Context();
		context.setVariable("patient",patient);

		String html = templateEngine.process("narrative",context);
		System.out.println(html);

		/*
		 HAPI-FHIR 라이브러리를 사용하여 Thymeleaf 템플릿 엔진으로 나만의 Narrative 템플릿을 만드는 방법에 대한 예제 코드:
			* HAPI-FHIR 라이브러리를 사용하여 Thymeleaf 템플릿 엔진으로 나만의 Narrative 템플릿을 만드는 방법에 대한 예제 코드:
			* 버전을 사용하며, 로컬 환경에서 테스트합니다.
			* Narrative 템플릿은 resources 폴더에 "narrative.html"로 생성합니다.
			* FhirContext를 사용하여 FHIR 버전에 맞는 컨텍스트를 생성합니다.
			* Patient 객체를 생성하고 필요한 데이터를 설정합니다.
			* Thymeleaf 템플릿 파일 "narrative.html"을 생성하여 필요한 데이터를 채워넣습니다.
			* Thymeleaf 템플릿 엔진을 사용하여 HTML을 생성하고 출력합니다.


		 Java 코드에서 Thymeleaf를 사용하여 HTML 생성:
		 	* ClassLoaderTemplateResolver를 사용하여 Thymeleaf 템플릿 파일을 로드합니다.
		 	* TemplateEngine을 초기화하고 Thymeleaf 컨텍스트에 데이터를 추가합니다.
		 	* Thymeleaf 템플릿 엔진으로 HTML을 생성하고 출력합니다.
		 	* Thymeleaf의 Context 클래스:
		 	* Context 클래스는 Thymeleaf의 컨텍스트(데이터 컨테이너)를 나타내는 클래스입니다.
		 	* 템플릿 엔진에서 템플릿과 데이터를 결합하여 최종 결과를 생성하는 데 사용됩니다.
		 	* Context 객체를 사용하여 템플릿에서 사용할 데이터를 설정할 수 있으며, 데이터는 setVariable() 메서드를 사용하여 설정합니다.
		 	* ClassLoaderTemplateResolver의 setPrefix("customizedTemplate/") 메서드:
		 	* setPrefix() 메서드는 템플릿 파일이 위치한 디렉토리의 경로를 설정하는 메서드입니다.
		 	* ClassLoaderTemplateResolver는 클래스패스를 기준으로 템플릿 파일을 로드합니다.
		 	* setPrefix("customizedTemplate/")는 customizedTemplate 폴더를 템플릿 파일이 위치한 디렉토리로 지정합니다.
		* */
	}
}
