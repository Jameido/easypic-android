package pro.eluzivespikes.easypic

import kotlin.collections.ArrayList

/**
 * Created by Luca Rossi on 07/05/2018.
 */
data class MissingPermissions(val missingPermissions: ArrayList<String>, var askRationale: Boolean) {
}