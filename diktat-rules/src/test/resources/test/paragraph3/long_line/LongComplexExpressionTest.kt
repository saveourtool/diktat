package test.paragraph3.long_line

fun foo(){
    val attrs.disabled = (selfRole == Role.OWNER && isSelfRecord(props.selfUserInfo, user)) || !(selfRole.isHigherOrEqualThan(Role.OWNER) || userRole.isLowerThan(selfRole))
}

val attrs.disabled = (selfRole == Role.OWNER && isSelfRecord(props.selfUserInfo, user)) || !(selfRole.isHigherOrEqualThan(Role.OWNER) || userRole.isLowerThan(selfRole))
