import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

public class AvroSchemaChecker {

    public static void main(String[] args) {
        // Sample RegReport Avro schema
        Schema regReportSchema = getRegReportSchema();

        // Field names to check
        List<String> fieldsToCheck = Arrays.asList("id", "qty", "tradetime", "mifid", "rate", "status");

        // Check if fields are present in the schema
        for (String fieldName : fieldsToCheck) {
            if (isFieldPresent(regReportSchema, fieldName)) {
                System.out.println("Field '" + fieldName + "' is present in the schema.");
            } else {
                System.out.println("Field '" + fieldName + "' is NOT present in the schema.");
            }
        }
    }

    private static Schema getRegReportSchema() {
        // Define your RegReport Avro schema here
        // This is a simplified example, you should replace it with your actual schema
        String schemaString = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"RegReport\",\n" +
                "  \"fields\": [\n" +
                "    {\"name\": \"id\", \"type\": \"string\"},\n" +
                "    {\"name\": \"qty\", \"type\": \"int\"},\n" +
                "    {\"name\": \"tradetime\", \"type\": \"long\"},\n" +
                "    {\"name\": \"mifid\", \"type\": \"string\"},\n" +
                "    {\"name\": \"rate\", \"type\": \"double\"},\n" +
                "    {\"name\": \"status\", \"type\": [\"null\", \"string\"], \"default\": null}\n" +
                "  ]\n" +
                "}";
        return new Schema.Parser().parse(schemaString);
    }

    private static boolean isFieldPresent(Schema schema, String fieldName) {
        for (Field field : schema.getFields()) {
            if (field.name().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }
}
