Certainly! Below is an example of a Kafka Streams application that processes `TxnReport` records, retrieves regulator configurations from a "reg-config-topic," and creates `RegReport` objects based on the configurations.

Please note that this is a simplified example, and you may need to adjust it based on your exact Avro serialization/deserialization, Kafka Streams setup, and other application-specific details.

```java
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegulatorProcessor {

    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "regulator-processor");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        StreamsBuilder builder = new StreamsBuilder();

        // Consume txnreport-topic
        KStream<String, TxnReport> txnReports = builder.stream("txnreport-topic",
                Consumed.with(Serdes.String(), new AvroSerde<>(TxnReport.class)));

        // Create a state store for regulator configurations
        StoreBuilder<KeyValueStore<String, List<String>>> storeBuilder =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore("regulatorConfigStore"),
                        Serdes.String(),
                        new AvroSerde<>(List.class)
                );
        builder.addStateStore(storeBuilder);

        // Apply the regulator transformer
        txnReports.transformValues(() -> new RegulatorTransformer(), "regulatorConfigStore")
                .to("output-topic", Produced.with(Serdes.String(), new AvroSerde<>(RegReport.class)));

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();
    }

    public static class RegulatorTransformer implements Transformer<String, TxnReport, Iterable<RegReport>> {

        private ProcessorContext context;
        private KeyValueStore<String, List<String>> regulatorConfigStore;

        @Override
        public void init(ProcessorContext context) {
            this.context = context;
            this.regulatorConfigStore = (KeyValueStore<String, List<String>>) context.getStateStore("regulatorConfigStore");
        }

        @Override
        public Iterable<RegReport> transform(String key, TxnReport txnReport) {
            List<String> regulatorNames = regulatorConfigStore.all();
            List<RegReport> regReports = new ArrayList<>();

            for (String regulatorName : regulatorNames) {
                List<String> fields = regulatorConfigStore.get(regulatorName);

                // Create a RegReport for each regulator
                RegReport regReport = createRegReport(txnReport, fields, regulatorName);
                regReports.add(regReport);
            }

            return regReports;
        }

        @Override
        public void close() {
            // Perform any necessary cleanup
        }

        private RegReport createRegReport(TxnReport txnReport, List<String> fields, String regulatorName) {
            RegReport regReport = new RegReport();

            for (String field : fields) {
                try {
                    Field txnReportField = TxnReport.class.getDeclaredField(field);
                    txnReportField.setAccessible(true);

                    Field regReportField = RegReport.class.getDeclaredField(field);
                    regReportField.setAccessible(true);

                    regReportField.set(regReport, txnReportField.get(txnReport));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    System.err.println("Exception processing field '" + field + "' for regulator: " + regulatorName);
                }
            }

            return regReport;
        }
    }
}

StreamsBuilder builder = new StreamsBuilder();

// Consume reg-config-topic
KStream<String, List<String>> regulatorConfigurations = builder.stream("reg-config-topic",
        Consumed.with(Serdes.String(), new AvroSerde<>(List.class)));

// Populate the regulatorConfigStore state store
regulatorConfigurations.foreach((regulatorName, fields) ->
        regulatorConfigStore.put(regulatorName, fields)
);

// ... (rest of the code)

KafkaStreams streams = new KafkaStreams(builder.build(), config);
streams.start();



This example assumes that you have appropriate Avro serialization/deserialization classes (`AvroSerde`) and that you've defined the `TxnReport` and `RegReport` classes with their corresponding Avro schemas. Adjust the code based on your actual setup and requirements.