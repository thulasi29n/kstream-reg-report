Map<String, List<String>> regulatorConfigMap = new HashMap<>();

// Step 2: Read Regulator Configurations into a Map
consumeRegConfigTopic((regulatorName, fields) -> regulatorConfigMap.put(regulatorName, fields));

// Step 3: Process TxnReport Records
consumeTxnReportTopic((txnReportId, txnReport) -> {
    // Step 4: For Each Regulator Name
    for (String regulatorName : regulatorConfigMap.keySet()) {
        // Step 4 (cont.): Retrieve Fields and Create RegReport
        List<String> fields = regulatorConfigMap.get(regulatorName);
        RegReport regReport = createRegReport(txnReport, fields);

        // Step 5: Output RegReport Objects
        sendRegReportToDownstream(regulatorName, regReport);
    }
});
