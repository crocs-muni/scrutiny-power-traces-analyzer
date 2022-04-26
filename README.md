# SCRUTINY power traces analyzer

The SCRUTINY power traces analyzer was implemented as a part of a diploma thesis Forensic profiles of certified cryptographic smartcards. The tool is a module integrated with [the SCRUTINY project](https://github.com/crocs-muni/scrutiny) [1]. The tool is implemented for anaylizing of power traces of cryptographic operations measured on smart cards. SCRUTINY power traces analyzer has 3 fuctionalities: Traces comparer, Trace classifier and CO template finder.

## Traces comparer

Traces comparer serves to create refrence profiles of smart cards based on the measured power traces and also has functionality to compare the reference profile with newly measured profile. The reference profile represents the correct power traces of power traces measured by some trusted authority or laboratory. The end-user measures power traces of the cryptographic operations on the purchased smart card. The user can use [the dedicated applet](https://github.com/crocs-muni/SPA-JavaCard-Applet) and [the cryptographic operations extractor](https://github.com/crocs-muni/SPA-Cryptographic-Operations-Extractor) [2]. After extraction of the of the power traces the user can compare his operations with the reference.

```
createref
    -c Path to the configuration.
    -o Output folder.
```

```
compare
    -c Path to the reference profile JSON.
    -n Path to the new card configuration, which the user wants to compare.
```

### Traces comparer input JSON

| **Parameter**  | **Profile** | **Description** |
| -------------- | ----------- | --------------- |
| _card_code_ | R/C | Unique identification of the card. |
| _pipelines_ | R | Code names of pipelines used to preprocess and compare the operations’ power traces (pep, pcp, pdtwp). |
| _custom_parameters_ | R | Additional parameters, structured as a list of parameter name and value pairs, that can be passed to the pipelines used in the context of the whole card. |
| _operations_ | R/C | Defined cryptographic operations that should be used to create reference profile or comparison. |
| _created_by_ | R | Information about the creator of the reference or comparison profile. |
| _additional_info_ | R | Additional information about the profile. |
| **Attribute _operations_** |
| _operation_code_ | R/C | Unique identification of cryptographic operation usually the name of the Java Card crypto algorithm. |
| _custom_parameters_ | R | Additional parameters that can be passed to the pipelines and override the more general parameters, used in the context of the operation. |
| _file_paths_ | R/C | Names of files that must be in the in the same folder as the traces. |
| **Attribute _custom_parameters_** |
| _parameters_ | R | List containing parameter and value pairs in string form. |

### Traces comparer output JSON

| **Parameter**  | **Profile** | **Description** |
| -------------- | ----------- | --------------- |
| card_code | R | Unique identification of the smart card passed from the input. |
| custom_parameters | R | Additional parameters used in the comparison pipelines, taken from the input. |
| results | R/C | Resulting comparisons between defined cryptographic operations that are used to create the reference or comparison profile. |
| created_date | R | Date when was the reference profile created. |
| created_by | R | Information about the creator of the reference or comparison profile. |
| additional_info | R | Additional information about the pro-file. |
| **Attribute _results_** |
| _operation_code_ | R/C | Unique identification of cryptographic operation from the input configuration. |
| _custom_parameters_ | R | Additional parameters taken from the input, overriding the global custom parameters. |
| _comparisons_ | R/C | Comparisons of each operation operation. |
| _operation_traces_paths_ | R/C | Names of files that must be in the in the same folder as the traces. |
| _execution_times_ | R/C | Execution times of the operation. |
| _operation_present_ | C | Determines if the operation is present during comparison process if the operation is in the reference profile. |
| **Attribute _comparisons_ for one particular operation** |
| _pipeline_ | R/C | Pipeline code name. |
| _metric_type_ | R/C | Distance metric or correlation. |
| _pipeline_comparisons_ | R/C | Comparisons of one particular operation for each pipeline. |
| **Attribute _execution_times_** |
| _unit_ | R/C | Unit of the execution time (ms). |
| _time_ | R/C | Time of the operation’s execution. |
| **Attribute _pipeline_comparisons_ of one particular pipeline** |
| _distance_ | R/C | Output number of the comparison step for the pipeline. |
| _file_path_ | R/C | Path to image which visualizes the comparison. |

## Trace classifier

```
classify
    -t Path to the trace to classify.
    -c Path to the configuration.
    -p Probability value for t-distribution.
    -j Sliding window jump to make similarity search faster.
    -g Flag to use graphic card.
```

### Trace classifier output

| **Parameter** | **Description** |
| ------------- | --------------- |
| _card_code_ | Unique identification of the smart card analyzed reference profile. |
| _operations_results_ | Result for each operation. |
| **Attribute _operations_results_ for one particular operation** |
| _operation_code_ | Unique identification of operation  |
| _similarity_intervals_ | Intervals in which the operations are recognized. |
| _visualized_operations_ | Path to the image, where are the operations highlighted on the trace. |
| **Attribute _similarity_intervals_ for one particular operation** |
| _similarity_value_ | Result of the comparison pipeline. |
| _similarity_value_type_ | Type of the comparison pipeline. |
| _time_from_ | Beginning of the time interval. |
| _time_to_ | End of the time interval. |
| _indexes_compared_ | Count of the compared indexes. |

## CO template finder

```
cotemp
    -t Path to the trace to analyze.
    -c Path to the configuration.
    -g Flag to use graphic card.
    -j Sliding window jump to make similarity search faster.
```

```
peaks
    -c Path to the configuration which is output of command cotemp.
    -n Expected number of cryptographic operation based on created template.
    -p Probability value of the t-distribution.
    -j Same jump parameter as in the cotemp command.
```


### CO template finder _cotemp_ action input JSON

| **Parameter** | **Description** |
| ------------- | --------------- |
| _mask_ | The pattern that represents the expected order of subsequences (i.e. repeating same subsequence four times XXXX). |
| _mask_elements_ | Specification of the separate elements of the mask. |
| **Attribute _mask_elements_ for one particular element** |
| _mask_element_ | Character representing subsequence of the mask. |
| _times_ | Possible lengths of the subsequence. |

### CO template finder _cotemp_ action output JSON

| **Parameter** | **Description** |
| ------------- | --------------- |
| _ideal_operation_length_time_ | Execution time of the template. |
| _ideal_operation_length_ | Number of template samples. |
| _real_trace_path_ | Path to the whole trace. |
| _operation_template_path_ | Path to the operation template. |
| _template_image_path_ | Path to the image of operation template. |
| _partial_results_ | Paths to the partial results visualizations. |
| _used_config_ | Input configuration with structure from the input. |

### CO template finder _peaks_ action output JSON

| **Parameter** | **Description** |
| ------------- | --------------- |
| is_match | Boolean value whether the expected number of operations is equals to the found number of operations. |
| expected_n | Expected number of operations. |
| real_n | Real number of operations. |
| all_candidates | Correlations of all candidates. |
| chosen_candidates | Correlations of chosen candidates based on the confidence intervals. |
| confidence_coefficient | User defined probability value of the distribution function. |
| confidence_interval_lower_bound | Lower bound of the confidence interval. |
| starting_times | Starting times of the chosen candidates. |
| n_visualizations_image_path | Path to file with visualized |
| cotemp_correlation_path | Correlation for each starting index of the sliding window CSV file. |
| cotemp_correlation_image_path | Path to visualization of the correlations. |



---
[1] https://is.muni.cz/auth/th/g7q67/

[2] https://is.muni.cz/auth/th/vbw5h/
