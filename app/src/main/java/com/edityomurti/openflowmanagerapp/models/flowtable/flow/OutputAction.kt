package com.edityomurti.openflowmanagerapp.models.flowtable.flow

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class OutputAction(
        @SerializedName("max-length")
        var maxLength: Int?,
        @SerializedName("output-node-connector")
        var outputNodeConnector: String?
): Serializable