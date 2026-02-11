| Mapping Name | FHIR -> openEHR (write) | openEHR -> FHIR (read) | Notes / Conditions |
|---|---|---|---|
| `dosageInstructionText` | `Dosage.text` -> `dosierung/items[at0178]` (`Dosierung Freitext`) | `dosierung/items[at0178]` -> `Dosage.text` | Direct mapping from `dosage.v2.yml`. |
| `doseQuantityValue` | `Dosage.doseAndRate.dose as Quantity` -> `dosierung/items[at0144]` (`Dosis`, `DV_QUANTITY`) | `dosierung/items[at0144]` quantity -> `Dosage.doseAndRate.doseQuantity` | Direct quantity mapping. |
| `doseRangeValue` (`dosageQuantityToRange`) | `Dosage.doseAndRate.dose as Range` -> `dosierung/items[at0144]` (`interval<dv_quantity>`) | `at0144` interval/quantity -> `Dosage.doseAndRate.doseRange` or `doseQuantity` (based on source/fhirPath) | If range exists, quantity keys are cleared on write. |
| `sequence` | `Dosage.sequence` -> `dosierung/items[at0164]` | `dosierung/items[at0164]` -> `Dosage.sequence` | Direct mapping. |
| `rateQuantity` | `Dosage.doseAndRate.rate as Quantity` -> `dosierung/items[at0134]` (`Verabreichungsrate`, `DV_QUANTITY`) | `dosierung/items[at0134]` quantity -> `Dosage.doseAndRate.rateQuantity` | Direct mapping row in model. |
| `rateRatio` (`ratio_to_dosage`) | `Dosage.doseAndRate.rate as Ratio` -> `at0134` + `at0102` | `at0134` + `at0102` -> `Dosage.doseAndRate.rateRatio` | Write: computes rate quantity from ratio and writes `verabreichungsdauer` from denominator duration. `verabreichungsrate` only written if unit is one of `l/h`, `ml/h`, `ml/s`, `ml/min` (normalized lowercase). Read: if rate+duration present, numerator = `rate * duration`; if rate missing but `dosis` + duration present, numerator is `dosis`; denominator always from duration (`min/h/d/s`). |
| `rateRangeValue` (`rangeToText`) | `Dosage.doseAndRate.rate as Range` -> `dosierung/items[at0134]/text_value` | `text_value` (or text-like source) at `at0134` -> `Dosage.doseAndRate.rateRange` | Serialized as readable text (e.g. `150-300 ml/h`), units normalized to lowercase. Not written to `quantity_value`. |
| `duration` (`dosageDurationToAdministrationDuration`) | `Dosage.timing.repeat` (duration+unit) -> `dosierung/items[at0102]` (`Verabreichungsdauer`, ISO duration) | `at0102` -> `Dosage.timing.repeat.duration(+durationMax)` | Supported units for duration write/read: `s`, `min`, `h`, `d`. |
| `dosageDailyTiming` (`timingToDaily`) | `Dosage.timing` with `periodUnit in [s,min,h,d]` -> slot `timing_daily.v1` | timing daily slot values -> `Dosage.timing` | Writes frequency (`frequenz`), period (`periode`), timeOfDay (`zeitpunkt`), event timing (`bestimmtes_ereignis`). |
| `dosageTimingNonDaily` (`timingNonDaily`) | `Dosage.timing` with `periodUnit not in [s,min,h,d]` -> slot `timing_nondaily.v1` | timing non-daily slot values -> `Dosage.timing` | Uses same timing reconstruction logic with non-daily source paths. |

## Path keys used by custom dosage mapper (flat openEHR)

| openEHR flat key (representative) | FHIR target/source |
|---|---|
| `.../dosis/quantity_value|magnitude|unit|code` | `Dosage.doseAndRate.doseQuantity` and ratio fallback numerator source |
| `.../verabreichungsrate/quantity_value|magnitude|unit` | `Dosage.doseAndRate.rateQuantity` / ratio source |
| `.../verabreichungsrate/text_value` | `Dosage.doseAndRate.rateRange` (via `rangeToText`) |
| `.../verabreichungsdauer` or `.../verabreichungsdauer|value` | `Dosage.timing.repeat.duration*` and ratio denominator |
