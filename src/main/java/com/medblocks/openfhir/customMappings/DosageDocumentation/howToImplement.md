# Dosage Field Mapping (FHIR <-> openEHR)

| FHIR field | openEHR field | Comment |
|---|---|---|
| `Dosage.doseAndRate.doseQuantity` (`$fhirRoot.dose`) | `CLUSTER.dosage.v2/items[at0144]` (`dose`) | Direct YAML mapping (`doseQuantityValue`). |
| `Dosage.text` (`$fhirRoot.text`) | `CLUSTER.dosage.v2/items[at0178]` (`dose_description`) | Direct YAML mapping (`doseDescription`). |
| `Dosage.doseAndRate.rateQuantity` (`$fhirRoot.rate.as(Quantity)`) | `CLUSTER.dosage.v2/items[at0134]` (`administration_rate`) | Direct YAML mapping (`rateQuantity`). |
| `Dosage.doseAndRate.doseRange` (`$fhirRoot.dose as Range`) | `.../dose/interval<dv_quantity>_value` | Supports interval representation for dose ranges. |
| `Dosage.doseAndRate.rateRange` (`$fhirRoot.rate as Range`) | `.../administration_rate/text_value` | Supports textual range representation for rates. |
| `Dosage.doseAndRate.rateRatio` (`$fhirRoot.rate as Ratio`) | `.../administration_rate/quantity_value` + `.../administration_duration` | Supports ratio as rate + duration representation. |
| `Dosage.doseAndRate.rateRatio` (`$fhirRoot.rate as Ratio`) | `CLUSTER.dosage.v2/items[at0134]` as merged quantity unit (`num/den`) | Fallback ratio representation via single DV_QUANTITY. |
| `Dosage.timing.repeat.duration` / `durationMax` | `.../administration_duration` | Duration conversion to/from ISO duration. |
| `Dosage.timing` (daily cadence) | `CLUSTER.timing_daily.v1` slot (frequency/period/time/event) | Daily timing slot mapping. |
| `Dosage.timing` (non-daily cadence) | `CLUSTER.timing_nondaily.v1` slot | Non-daily timing slot mapping. |
| `Dosage.sequence` | `CLUSTER.dosage.v2/items[at0164]` | Supported by dosage custom mapper path handling; sequence remains scalar/direct. |

## Notes

- Baseline YAML file: `src/test/resources/kds_new/model/cluster/org.openehr/dosage.v2.BackboneElement.yml`
- Custom mapping implementation: `src/main/java/com/medblocks/openfhir/customMappings/DosageCustomMappings.java`
- `rateRatio` is not active as direct YAML row and is handled via custom mappings.

## Custom mappings summary

- `dosageQuantityToRange`:
  - FHIR -> openEHR: `Dosage.doseAndRate.doseRange.low/high` -> `.../dose/interval<dv_quantity>_value/lower|upper`.
  - FHIR -> openEHR: `Dosage.doseAndRate.doseQuantity` -> `.../dose/quantity_value`.
  - Behavior: if a range is present, quantity keys are removed to avoid mixed interval+scalar dose.
- `rangeToText`:
  - FHIR -> openEHR: `Dosage.doseAndRate.rateRange.low/high + unit` -> `.../administration_rate/text_value`.
  - Calculation: serializes to a human-readable string (for example `150-300 ml/h`).
- `ratio_to_dosage`:
  - FHIR -> openEHR: `Dosage.doseAndRate.rateRatio.numerator` + `denominator` -> `.../administration_rate/quantity_value` + `.../administration_duration`.
  - Calculation (write): `rate = numerator.value / denominator.value`.
  - Calculation (write): `duration` is derived from denominator (`s|min|h|d`) and written as ISO duration to `administration_duration`.
  - Calculation (read): reconstruct `rateRatio` from `rate * duration`; if rate is missing, use `dose` as numerator fallback.
- `ratio_to_dv_quantity`:
  - FHIR -> openEHR: `Dosage.doseAndRate.rateRatio` -> `CLUSTER.dosage.v2/items[at0134]`.
  - Calculation: keeps numerator magnitude and builds unit/code as `numeratorUnit/denominatorUnit`.
- `dosageDurationToAdministrationDuration`:
  - FHIR -> openEHR: `Dosage.timing.repeat.duration` (+ unit) -> `.../administration_duration`.
  - openEHR -> FHIR: `.../administration_duration` -> `Dosage.timing.repeat.duration` / `durationMax`.
  - Calculation: numeric duration + unit converted to ISO duration and back (`s|min|h|d`).
- `timingToDaily`:
  - FHIR -> openEHR: `Dosage.timing.repeat.frequency`, `frequencyMax`, `period`, `periodMax`, `periodUnit`, `timeOfDay`, `when`
    -> daily slot keys (`frequency`, `period`, `administration_time_interval`, `specific_event`).
  - Calculation: frequency/period values are normalized into daily timing structure.
- `timingNonDaily`:
  - FHIR -> openEHR: same `Dosage.timing.repeat` inputs as above, for non-daily period units
    -> non-daily slot keys.
  - Calculation: same timing normalization logic as daily, but written to non-daily slot.

## Coverage of `MedicationStatement.dosage` (DosageDE)

Classification basis:

- `src/test/resources/kds_new/model/observation/org.openehr/medication_statement.v0.yml`
- `src/test/resources/kds_new/model/cluster/org.openehr/dosage.v2.yml`
- `src/test/resources/kds_new/model/cluster/org.openehr/timing_daily.v1.yml`
- `src/test/resources/kds_new/model/cluster/org.openehr/timing_non_daily.yml`
- `src/main/java/com/medblocks/openfhir/customMappings/DosageCustomMappings.java`

### Mapped

| FHIR field | Status | Notes |
|---|---|---|
| `dosage` | mapped | `MedicationStatement.dosage` is mapped into `CLUSTER.dosage.v2` slot. |
| `sequence` | mapped | Direct mapping to `items[at0164]`. |
| `text` | mapped | Direct mapping to `items[at0178]` (dose description). |
| `timing` | mapped | Routed to daily or non-daily timing cluster via conditions. |
| `timing.repeat` | mapped | Used as source for timing custom mappings. |
| `timing.repeat.duration` | mapped | Mapped via duration custom mapping to administration duration. |
| `timing.repeat.durationMax` | mapped | Read/write support through duration handling. |
| `timing.repeat.durationUnit` | mapped | Used for ISO duration conversion. |
| `timing.repeat.frequency` | mapped | Mapped in timing custom mapping. |
| `timing.repeat.frequencyMax` | mapped | Mapped in timing custom mapping. |
| `timing.repeat.period` | mapped | Mapped in timing custom mapping. |
| `timing.repeat.periodMax` | mapped | Mapped in timing custom mapping. |
| `timing.repeat.periodUnit` | mapped | Controls daily vs non-daily routing and period conversion. |
| `timing.repeat.timeOfDay` | mapped | Mapped to timing slot (`administration_time` path family). |
| `timing.repeat.when` | mapped | Explicit row in timing daily/non-daily models. |
| `timing.repeat.offset` | mapped | Explicit row in timing daily/non-daily models. |
| `doseAndRate` | mapped | Parent for dose/rate mappings in `dosage.v2.yml`. |
| `dose[x]` | mapped | Quantity and Range are supported. |
| `doseQuantity` | mapped | Direct quantity mapping. |
| `doseRange` | mapped | Interval mapping via custom mapper. |
| `rate[x]` | mapped | Quantity, Ratio, and Range are supported. |
| `rateRatio` | mapped | Custom mapping (`ratio_to_dosage` / fallback). |
| `rateRange` | mapped | Custom text-based range mapping. |
| `rateQuantity` | mapped | Direct quantity mapping. |

### Partly mapped

| FHIR field | Status | Notes |
|---|---|---|
| `asNeeded[x]` | partly mapped | Implemented in `timing_daily.v1`; no equivalent row in `timing_non_daily.yml`. |
| `asNeededBoolean` | partly mapped | Same limitation as above. |
| `asNeededCodeableConcept` | partly mapped | Same limitation as above. |
| `route` | partly mapped | `dosage.route` is mapped at MedicationStatement event level, but not fully decomposed into all profile slices. |
| `route.coding` | partly mapped | CodeableConcept is transferred, but slicing-specific constraints (EDQM/SNOMED cardinalities) are not explicitly mapped field-by-field. |
| `route.text` | partly mapped | May be preserved through concept mapping, but no dedicated text-only rule. |
| `doseAndRate.rateRatio.numerator` | partly mapped | Reconstructed/calculated; exact structure depends on ratio conversion path. |
| `doseAndRate.rateRatio.denominator` | partly mapped | Reconstructed from administration duration; normalized units (`s|min|h|d`). |
| `value/unit/system/code` on quantity-like children | partly mapped | Present for mapped quantity fields, but only where source type/path is supported. |

### Not mapped

| FHIR field | Status | Notes |
|---|---|---|
| `additionalInstruction` | not mapped | No mapping row. |
| `patientInstruction` | not mapped | No mapping row. |
| `timing.event` | not mapped | No explicit event[] mapping rule in dosage/timing models. |
| `timing.repeat.bounds[x]` | not mapped | No bounds mapping (Duration/Range/Period bounds). |
| `timing.repeat.boundsDuration` | not mapped | No mapping rule. |
| `timing.repeat.boundsRange` | not mapped | No mapping rule. |
| `timing.repeat.boundsPeriod` | not mapped | No mapping rule. |
| `timing.repeat.count` | not mapped | No mapping rule. |
| `timing.repeat.countMax` | not mapped | No mapping rule. |
| `timing.repeat.dayOfWeek` | not mapped | No mapping rule. |
| `code` (dosage-level codeable concept) | not mapped | No mapping row for this field in `dosage.v2.yml`. |
| `site` | not mapped | No row for `dosage.site` in current MedicationStatement dosage mappings. |
| `site.coding` | not mapped | No mapping rule. |
| `site.text` | not mapped | No mapping rule. |
| `method` | not mapped | No mapping row for `dosage.method`. |
| `doseAndRate.type` | not mapped | No mapping row for `doseAndRate.type`. |
| `maxDosePerPeriod` | not mapped | No mapping row. |
| `maxDosePerPeriod.numerator` | not mapped | No mapping row. |
| `maxDosePerPeriod.denominator` | not mapped | No mapping row. |
| `maxDosePerAdministration` | not mapped | No mapping row. |
| `maxDosePerAdministration.value/unit/system/code` | not mapped | No mapping row. |
| `maxDosePerLifetime` | not mapped | No mapping row. |
