package kr.co.wground.track.presentation.request.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class TrackScheduleValidator : ConstraintValidator<ValidTrackDate, TrackDate> {
    override fun isValid(value: TrackDate?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true

        val (start, end) = value.startDate to value.endDate

        if (start == null || end == null) return true

        val valid = !end.isBefore(start)

        if (!valid) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate(context.defaultConstraintMessageTemplate)
                .addPropertyNode("endDate")
                .addConstraintViolation()
        }
        return valid
    }
}