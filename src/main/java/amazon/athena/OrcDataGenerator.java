package amazon.athena;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.OrcFile;
import org.apache.orc.TypeDescription;
import org.apache.orc.impl.WriterImpl;

public class OrcDataGenerator {

  public static void main(String[] args) throws IOException {
    generateNewOrcFile();
  }

  private static void generateNewOrcFile() throws IOException {
    Configuration conf = new Configuration();
    TypeDescription schema = TypeDescription.createStruct()
        .addField("id", TypeDescription.createString())
        .addField("firstname", TypeDescription.createString())
        .addField("lastname", TypeDescription.createString())
        .addField("company", TypeDescription.createString())
        .addField("paddress", TypeDescription.createString())
        .addField("facttheylike", TypeDescription.createString());
    Path outputFile = new Path("people.orc");
    WriterImpl writer = (WriterImpl) OrcFile.createWriter(
        outputFile,
        OrcFile.writerOptions(conf)
            .fileSystem(FileSystem.getLocal(conf))
            .setSchema(schema)
            .overwrite(true));
    VectorizedRowBatch vectorizedRowBatch = schema.createRowBatch(10000);

    BytesColumnVector idBCV = (BytesColumnVector) vectorizedRowBatch.cols[0];
    BytesColumnVector firstNameBCV = (BytesColumnVector) vectorizedRowBatch.cols[1];
    BytesColumnVector lastNameBCV = (BytesColumnVector) vectorizedRowBatch.cols[2];
    BytesColumnVector companyBCV = (BytesColumnVector) vectorizedRowBatch.cols[3];
    BytesColumnVector paddressBCV = (BytesColumnVector) vectorizedRowBatch.cols[4];
    BytesColumnVector facttheylikeBCV = (BytesColumnVector) vectorizedRowBatch.cols[5];

    Faker faker = new Faker();
    for (int i = 0; i < 10000; i++) {
      vectorizedRowBatch.size++;
      Name name = faker.name();
      idBCV.setVal(i, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
      firstNameBCV.setVal(i, name.firstName().getBytes(StandardCharsets.UTF_8));
      lastNameBCV.setVal(i, name.lastName().getBytes(StandardCharsets.UTF_8));
      companyBCV.setVal(i, faker.company().name().getBytes(StandardCharsets.UTF_8));
      paddressBCV.setVal(i, faker.address().fullAddress().getBytes(StandardCharsets.UTF_8));
      facttheylikeBCV.setVal(i, faker.chuckNorris().fact().getBytes(StandardCharsets.UTF_8));
    }

    writer.addRowBatch(vectorizedRowBatch);
    writer.close();
  }

}
