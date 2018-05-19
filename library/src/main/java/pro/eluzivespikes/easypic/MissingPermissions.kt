package pro.eluzivespikes.easypic

import kotlin.collections.ArrayList

/**
 * @author Jameido
 * @since 3
 *
 * Data class used to pass missing permissions
 */
data class MissingPermissions(val missingPermissions: ArrayList<String>, var askRationale: Boolean)