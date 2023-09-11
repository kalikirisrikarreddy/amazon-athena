package amazon.athena;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.javafaker.Address;
import com.github.javafaker.Company;
import com.github.javafaker.Faker;
import com.github.javafaker.Name;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class JsonDataProducer {

	private static final int NO_OF_RECS_PER_FILE = Integer.parseInt(System.getenv("NO_OF_RECS_PER_FILE"));
	private static final int FROM_YEAR = Integer.parseInt(System.getenv("FROM_YEAR"));
	private static final int TO_YEAR = Integer.parseInt(System.getenv("TO_YEAR"));
	private static final List<Employee> EMPLOYEES = new ArrayList<Employee>(NO_OF_RECS_PER_FILE);
	private static final String HYPHEN = "-";
	private static final ObjectWriter OBJECT_WRITER = new ObjectMapper().writerFor(List.class);

	static S3Client s3Client = S3Client.builder().region(Region.US_EAST_1)
			.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(null, null)))
			.overrideConfiguration(ClientOverrideConfiguration.builder().apiCallAttemptTimeout(Duration.ofMinutes(5L))
					.apiCallTimeout(Duration.ofMinutes(5L)).retryPolicy(RetryPolicy.builder(RetryMode.STANDARD).build())
					.build())
			.build();

	public static void main(String[] args)
			throws InterruptedException, StreamWriteException, DatabindException, IOException {
		Faker faker = new Faker();
		for (int recordIndex = 0; recordIndex < NO_OF_RECS_PER_FILE; recordIndex++) {
			Name name = faker.name();
			Company company = faker.company();
			Address address = faker.address();
			EMPLOYEES.add(new Employee(UUID.randomUUID().toString(), name.firstName() + HYPHEN,
					name.lastName() + HYPHEN, company.name(), address.fullAddress(), address.secondaryAddress()));
		}
		for (int year = FROM_YEAR; year <= TO_YEAR; year++) {
			for (int month = 1; month <= 12; month++) {
				final int y = year;
				final int m = month;
				int numberOfDaysInThisMonth = YearMonth.of(y, m).lengthOfMonth();
				for (int day = 1; day <= numberOfDaysInThisMonth; day++) {
					generateFileForADay(y, m, day);
				}
			}
		}
	}

	private static void generateFileForADay(final int y, final int m, int day) {
		String variableSuffix = String.valueOf(y) + String.valueOf(m) + String.valueOf(day);
		for (int recordIndex = 0; recordIndex < NO_OF_RECS_PER_FILE; recordIndex++) {
			Employee employee = EMPLOYEES.get(recordIndex);
			employee.setId(UUID.randomUUID().toString());
			employee.setFirstname(
					StringUtils.substringBefore(employee.getFirstname(), HYPHEN) + HYPHEN + variableSuffix);
			employee.setLastname(StringUtils.substringBefore(employee.getLastname(), HYPHEN) + HYPHEN + variableSuffix);
		}

		File file = new File("f" + LocalDate.of(y, m, day).format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv.gz");

		try (FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				GZIPOutputStream gos = new GZIPOutputStream(bos);) {
			OBJECT_WRITER.writeValue(gos, EMPLOYEES);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String s3Key = "birthday=" + LocalDate.of(y, m, day).format(DateTimeFormatter.ISO_LOCAL_DATE) + "/data.csv.gz";
		s3Client.putObject(PutObjectRequest.builder().bucket("athena-datasets-1").key(s3Key).build(),
				RequestBody.fromFile(file));
		file.delete();
	}

}
