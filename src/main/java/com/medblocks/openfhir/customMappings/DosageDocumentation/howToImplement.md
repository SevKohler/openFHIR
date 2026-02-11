Create four mappiong:




- name: "dosageTiming"
  with:
  fhir: "$fhirRoot.timing"
  openehr: "$archetype/items[openEHR-EHR-CLUSTER.timing_daily.v1]"
  mappingCode: "timingToDaily_NonDaily"

- name: "doseQuantityValue"
  with:
  fhir: "$fhirRoot.doseAndRate.dose.as(Quantity)"
  openehr: "$archetype/items[at0144]"
  mappingCode: "dosageQuantityToRange"

- name: "doseRangeValue"
  with:
  fhir: "$fhirRoot.doseAndRate.dose.as(Range)"
  openehr: "$archetype/items[at0144]"
  mappingCode: "dosageQuantityToRange"

- name: "rateRatio"
  with:
  fhir: "$fhirRoot.doseAndRate.rate.as(Ratio)"
  openehr: "$archetype/items[at0134]"
  mappingCode: "ratio_to_dv_quantity"

- name: "duration"
  with:
  fhir: "$fhirRoot.timing.repeat"
  openehr: "$archetype/items[at0102]"
  mappingCode: "dosageDurationToAdministrationDuration"

This should input and output to correspdiong data type


This is how the conversion should be done:
## Mapping: FHIR to openEHR

| openEHR Field (with Cluster Path) | FHIR Field(s) | Notes                                                                                                         |
|----------------------------------|---------------|---------------------------------------------------------------------------------------------------------------|
| `/items[at0144]` **Dosage quantity** | `doseQuantity.value`<br>`doseQuantity.unit`<br>**OR**<br>`doseRange.low`<br>`doseRange.high` | **Combined**: If `doseRange` exists, format as `low–high mg`, otherwise use `doseQuantity` as a single value. |
| `/items[at0134]` **Administration rate** | `rateRatio.numerator.value`<br>`rateRatio.numerator.unit`<br>`rateRatio.denominator.value`<br>`rateRatio.denominator.unit` | **Combined** to form `600 mg/h` or similar.                                                                   |
| `[openEHR-EHR-CLUSTER.timing_daily.v1]`<br>`/items[at0004]` **Specific time** | `timing.repeat.timeOfDay[0]` | Uses the first occurrence of `timeOfDay`.                                                                     |
| `[openEHR-EHR-CLUSTER.timing_daily.v1]`<br>`/items[at0003]` **Frequency** | `timing.repeat.frequency`<br>`timing.repeat.frequencyMax` | **Combined**: If `frequencyMax` exists, format as `frequency to frequencyMax times per [periodUnit]`.         |
| `[openEHR-EHR-CLUSTER.timing_daily.v1]`<br>`/items[at0014]` **Interval** | `timing.repeat.period`<br>`timing.repeat.periodMax`<br>`timing.repeat.periodUnit` | **Combined**: If `periodMax` exists, format as `every period–periodMax [unit]`.                               |
| `/items[at0164]` **Dosage sequence** | `timing.repeat.count` | Direct mapping.                                                                                               |
| `/items[at0102]` **Administration duration** | `timing.repeat.duration`<br>`timing.repeat.durationMax` | **Combined**: If `durationMax` exists, format as `duration–durationMax`.                                      |



## Notes

- If both `doseQuantity` and `doseRange` are present, `doseRange` takes priority for `[at0144]`.
- Allowed `periodUnit` values: `d`, `h`, `min`, `s`.  
  Any other values (e.g. weeks) should be mapped to a **non_dailycluster**.
- `period = 1` is expected when `periodUnit = d`, explicitly stated in the code.

---

## Implementation Guidelines

- Ensure period normalization (e.g. mapping hours properly into the **Interval** field).
- Use range notation (`low–high`) wherever applicable.
- Default to `1` when mapping daily dosages unless specified otherwise.

---

## Validation Rules

- Ensure no conflicting time units.
- Verify frequency aligns with period constraints.
- Convert units as needed while preserving accuracy.

### UCUM Duration → ISO 8601
> ISO 8601 duration pattern: P[nY][nM][nW][nD][T[nH][nM][nS]]

based on https://build.fhir.org/valueset-duration-units.html

| UCUM Unit | ISO 8601 Format Example | Description |
|----------|--------------------------|-------------|
| ms | PT0.001S | Milliseconds  |
| s | PT00S | Seconds |
| min | PT00M | Minutes |
| h | PT00H | Hours |
| d | P00D | Days |
| wk | P00W | Weeks  |
| mo | P00M | Months |
| a | P00Y | Years |