public class RegReportFactory {

    private final Map<String, List<String>> regulatorFieldMappings;

    public RegReportFactory(Map<String, List<String>> regulatorFieldMappings) {
        this.regulatorFieldMappings = regulatorFieldMappings;
    }

    public List<RegReport> createRegReports(TxnReport txnReport) {
        List<RegReport> regReports = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : regulatorFieldMappings.entrySet()) {
            String regulatorId = entry.getKey();
            List<String> regulatorFields = entry.getValue();

            RegReport regReport = createRegReport(txnReport, regulatorFields);
            regReports.add(regReport);
        }

        return regReports;
    }

    private RegReport createRegReport(TxnReport txnReport, List<String> fields) {
        RegReport regReport = new RegReport();

        for (String field : fields) {
            try {
                Field txnReportField = TxnReport.class.getDeclaredField(field);
                txnReportField.setAccessible(true);

                Field regReportField = RegReport.class.getDeclaredField(field);
                regReportField.setAccessible(true);

                regReportField.set(regReport, txnReportField.get(txnReport));
            } catch (Exception e) {
                // Handle exceptions as needed
                e.printStackTrace();
            }
        }

        return regReport;
    }
}

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Transformer;
import org.apache.kafka.streams.processor.TransformerSupplier;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.List;

public class RegReportTopology {

    public static void main(String[] args) {
        // Define the Kafka Streams configuration and builder
        // ...

        StreamsBuilder builder = new StreamsBuilder();

        // Define the input topic
        KStream<String, TxnReport> txnReportStream = builder.stream(
                "input-txn-report-topic",
                Consumed.with(AppSerdes.String(), AppSerdes.TxnReport())
        );

        // Use a transformer to transform TxnReport to List<RegReport> for each regulator
        txnReportStream.transformValues(new RegReportTransformerSupplier(), "regulator-field-store")
                .to("output-reg-report-topic", Produced.with(AppSerdes.String(), AppSerdes.ListOfRegReport()));

        // Build and start the Kafka Streams application
        // ...
    }

    private static class RegReportTransformerSupplier implements TransformerSupplier<TxnReport, List<RegReport>> {

        @Override
        public Transformer<TxnReport, List<RegReport>> get() {
            return new RegReportTransformer();
        }
    }

    private static class RegReportTransformer implements Transformer<TxnReport, List<RegReport>> {
        private KeyValueStore<String, List<String>> regulatorFieldStore;
        private RegReportFactory regReportFactory;

        @Override
        public void init(ProcessorContext context) {
            // Initialize RegReportFactory during the initialization phase
            this.regulatorFieldStore = (KeyValueStore<String, List<String>>) context.getStateStore("regulator-field-store");
            this.regReportFactory = new RegReportFactory(loadRegulatorFieldMappings());
        }

        @Override
        public List<RegReport> transform(TxnReport value) {
            // Use RegReportFactory to generate RegReport objects for each regulator
            return regReportFactory.createRegReports(value);
        }

        @Override
        public void close() {
            // Close resources if needed
        }
    }

    private static List<String> loadRegulatorFieldMappings() {
        // Load regulator field mappings from your configuration store
        // ...
        return /* List of regulator field mappings */;
    }
}

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Transformer;
import org.apache.kafka.streams.processor.TransformerSupplier;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.List;

public class RegReportTopology {

    public static void main(String[] args) {
        // Define the Kafka Streams configuration and builder
        // ...

        StreamsBuilder builder = new StreamsBuilder();

        // Define the input topic
        KStream<String, TxnReport> txnReportStream = builder.stream(
                "input-txn-report-topic",
                Consumed.with(AppSerdes.String(), AppSerdes.TxnReport())
        );

        // Use a transformer to transform TxnReport to List<RegReport> for each regulator
        txnReportStream.transformValues(new RegReportTransformerSupplier(), "regulator-field-store")
                .to("output-reg-report-topic", Produced.with(AppSerdes.String(), AppSerdes.ListOfRegReport()));

        // Build and start the Kafka Streams application
        // ...
    }

    private static class RegReportTransformerSupplier implements TransformerSupplier<String, TxnReport, List<RegReport>> {

        @Override
        public Transformer<String, TxnReport, List<RegReport>> get() {
            return new RegReportTransformer();
        }
    }

    private static class RegReportTransformer implements Transformer<String, TxnReport, List<RegReport>> {
        private KeyValueStore<String, List<String>> regulatorFieldStore;
        private RegReportFactory regReportFactory;

        @Override
        public void init(ProcessorContext context) {
            // Initialize RegReportFactory during the initialization phase
            this.regulatorFieldStore = (KeyValueStore<String, List<String>>) context.getStateStore("regulator-field-store");
            this.regReportFactory = new RegReportFactory();
        }

        @Override
        public List<RegReport> transform(String key, TxnReport value) {
            // Use submissionacntID to determine regulators
            List<String> regulators = getRegulatorsForSubmissionAcnt(value.getSubmissionAcntID());

            // Generate RegReport objects for each regulator
            List<RegReport> regReports = regReportFactory.createRegReportsForRegulators(value, regulators, regulatorFieldStore);

            return regReports;
        }

        @Override
        public void close() {
            // Close resources if needed
        }

        private List<String> getRegulatorsForSubmissionAcnt(String submissionAcntID) {
            // Logic to determine regulators based on submissionAcntID
            // ...
            return /* List of regulators */;
        }
    }
}

