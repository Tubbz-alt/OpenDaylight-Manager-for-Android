package com.edityomurti.openflowmanagerapp.ui.flow_add


import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner

import com.edityomurti.openflowmanagerapp.R
import com.edityomurti.openflowmanagerapp.models.FlowProperties
import com.edityomurti.openflowmanagerapp.models.flowtable.flow.*
import com.edityomurti.openflowmanagerapp.models.topology.NodeDataSerializable
import com.edityomurti.openflowmanagerapp.utils.Constants
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.fragment_add_flow_match.view.*
import kotlinx.android.synthetic.main.layout_match_ethernet_type.view.*
import kotlinx.android.synthetic.main.layout_match_in_port.view.*
import kotlinx.android.synthetic.main.layout_match_ipv4_dest.view.*
import kotlinx.android.synthetic.main.layout_match_ipv4_source.view.*
import kotlinx.android.synthetic.main.layout_match_mac_dest.view.*
import kotlinx.android.synthetic.main.layout_match_mac_source.view.*

class AddFlowMatchFragment : Fragment() {

    lateinit var mView: View

    var matchData: MutableList<FlowProperties> = ArrayList()
    var selectedMatchData: MutableList<FlowProperties> = ArrayList()

    val tag_match_inport = "match_inport"
    val tag_match_ehternettype = "match_ehternettype"
    val tag_match_macsource = "match_macsource"
    val tag_match_macdest = "match_macdest"
    val tag_match_ipv4source = "match_ipv4source"
    val tag_match_ipv4dest = "match_ipv4dest"

    var nodeData: NodeDataSerializable? = null
    var newFlow: Flow? = null

    var arrayAdapterInport: ArrayAdapter<String>? = null

    val VALUE_MAX = 65535
    val VALUE_CANT_BE_BLANK = "Cannot be blank"
    val VALUE_ERROR = "Value must between 0 - $VALUE_MAX"
    val VALUE_MAC_ADDR_ERROR = "INVALID MAC ADDRESS"
    val VALUE_IP_ADDR_ERROR = "Should have valid IP with netmask \"/\" separated"

    companion object {
        fun newInstance(nodeData: NodeDataSerializable): AddFlowMatchFragment{
            val args = Bundle()
            args.putSerializable(Constants.NODE_DATA, nodeData)

            var fragment = AddFlowMatchFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_add_flow_match, container, false)

        activity?.title = "Add Flow: Add Match"

        setData()

        if(arguments != null){
            var nodeList: ArrayList<String>? = null

            nodeData = arguments!!.getSerializable(Constants.NODE_DATA) as NodeDataSerializable
            for(i in nodeData!!.nodeSerializableData.indices){
                if(nodeData!!.nodeSerializableData.get(i).nodeId == newFlow?.nodeId){
                    nodeList = nodeData!!.nodeSerializableData.get(i).nodeConnector
                }
            }
            arrayAdapterInport = ArrayAdapter(context, R.layout.item_spinner, nodeList)
        }

        setDefaultData()

        var alertDialog = AlertDialog.Builder(context)
        var arrayAdapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1)
        for(i in matchData.indices){
            arrayAdapter.add(matchData[i].propName)
        }

            alertDialog.setAdapter(arrayAdapter) { dialog, which ->
            val view = inflater.inflate(matchData[which].layoutView, null, false)
            val btnDelete = view.findViewById<ImageView>(R.id.iv_delete)
            val match = matchData[which]

            if(match.propId == tag_match_inport){
                val spinnerInport = view.findViewById<Spinner>(R.id.spinner_in_port)
                spinnerInport.adapter = arrayAdapterInport
            }
//            else if(match.propId == tag_match_macsource){
//                val etMatchSource = view.findViewById<MaterialEditText>(R.id.et_mac_source)
//                etMatchSource.setAllCaps(true)
//                etMatchSource.addTextChangedListener(object : TextWatcher{
//                    override fun afterTextChanged(s: Editable?) {
//                    }
//
//                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                    }
//
//                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                        if(count == 2 ){
//                            etMatchSource.setText(etMatchSource.text.toString() + ":")
//                            etMatchSource.setSelection(etMatchSource.text.toString().length)
//                        }
//                    }
//
//                })
//            }
//            else if(match.propId == tag_match_macdest){
//                val etMatchDest = view.findViewById<MaterialEditText>(R.id.et_mac_dest)
//                etMatchDest.setAllCaps(true)
//                etMatchDest.addTextChangedListener(object : TextWatcher{
//                    override fun afterTextChanged(s: Editable?) {
//                    }
//
//                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                    }
//
//                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                        if(count == 2 ){
//                            etMatchDest.setText(etMatchDest.text.toString() + ":")
//                            etMatchDest.setSelection(etMatchDest.text.toString().length)
//                        }
//                    }
//                })
//            }

            btnDelete.setOnClickListener {
                (view.parent as LinearLayout).removeView(view)
                selectedMatchData.remove(match)
                matchData.add(match)
                arrayAdapter.add(match.propName)
                arrayAdapter.notifyDataSetChanged()
                if(arrayAdapter.count == 1){
                    mView.btn_add_match.visibility = View.VISIBLE
                }
            }

            selectedMatchData.add(match)
            arrayAdapter.remove(match.propName)
            arrayAdapter.notifyDataSetChanged()
            matchData.remove(match)

            mView.ll_match.addView(view)
            dialog.dismiss()

            if(arrayAdapter.count == 0){
                mView.btn_add_match.visibility = View.GONE
            } else {
                mView.btn_add_match.visibility = View.VISIBLE
            }
        }

        mView.btn_add_match.setOnClickListener {
            alertDialog.show()
        }

        return mView
    }

    fun setData(){
        (activity as AddFlowActivity).setDataStatus(false)
        newFlow = (activity as AddFlowActivity).getFlow()
        mView.tv_device.text = newFlow!!.nodeId
    }

    fun setDefaultData(){
        matchData.clear()
        selectedMatchData.clear()

        var inPortMatch = FlowProperties(tag_match_inport, "In port", R.layout.layout_match_in_port)
        matchData.add(inPortMatch)

        var ethernetType = FlowProperties(tag_match_ehternettype, "Ethernet type", R.layout.layout_match_ethernet_type)
        matchData.add(ethernetType)

        var macSource = FlowProperties(tag_match_macsource, "Source MAC", R.layout.layout_match_mac_source)
        matchData.add(macSource)

        var macDest = FlowProperties(tag_match_macdest, "Destination MAC", R.layout.layout_match_mac_dest)
        matchData.add(macDest)

        var ipv4Source = FlowProperties(tag_match_ipv4source, "IPv4 Source", R.layout.layout_match_ipv4_source)
        matchData.add(ipv4Source)

        var ipv4Dest = FlowProperties(tag_match_ipv4dest, "IPv4 Destination", R.layout.layout_match_ipv4_dest)
        matchData.add(ipv4Dest)
    }

    fun setFlow(): Flow {
        var isCompleted = true

        newFlow = (activity as AddFlowActivity).getFlow()

        var ethernetMatch: EthernetMatch? = null
        var match = Match(null, null, null, null)

        for(i in selectedMatchData.indices){
            when(selectedMatchData[i].propId){
                tag_match_inport -> {
                    match.inPort = mView.spinner_in_port.selectedItem.toString()
                }
                tag_match_ehternettype -> {
                    if(!mView.et_ethernet_type.text.isNullOrEmpty() && !mView.et_ethernet_type.text.isNullOrBlank()){
                        try{
                            val ethernetTypeValue = mView.et_ethernet_type.text.toString().toInt()
                            if(ethernetTypeValue in 0..VALUE_MAX){
                                mView.et_ethernet_type.error = null
                                var ethernetType = EthernetType(ethernetTypeValue)
                                if(ethernetMatch != null){
                                    ethernetMatch?.ethernetType = ethernetType
                                } else {
                                    ethernetMatch = EthernetMatch(null, null, ethernetType)
                                }
                            } else {
                                mView.et_ethernet_type.error = VALUE_ERROR
                                isCompleted = false
                            }
                        } catch (e: Exception){
                            mView.et_ethernet_type.error = VALUE_ERROR
                            isCompleted = false
                        }
                    } else {
                        mView.et_ethernet_type.error = VALUE_CANT_BE_BLANK
                        isCompleted = false
                    }

                }
                tag_match_macsource -> {
                    if(!mView.et_mac_source.text.isNullOrEmpty() && !mView.et_mac_source.text.isNullOrBlank() ){
                        val macSource = mView.et_mac_source.text.toString()
                        val colonCount = macSource.length - macSource.replace(":", "").length
                        if(macSource.length == 17 && colonCount == 5) {
                            mView.et_mac_source.error = null
                            var ethernetSource = EthernetSource(mView.et_mac_source.text.toString())
                            if (ethernetMatch != null) {
                                ethernetMatch?.ethernetSource = ethernetSource
                            } else {
                                ethernetMatch = EthernetMatch(ethernetSource, null, null)
                            }
                        } else {
                            mView.et_mac_source.error = VALUE_MAC_ADDR_ERROR
                            isCompleted = false
                        }
                    } else {
                        mView.et_mac_source.error = VALUE_CANT_BE_BLANK
                        isCompleted = false
                    }
                }
                tag_match_macdest -> {
                    if(!mView.et_mac_dest.text.isNullOrEmpty() && !mView.et_mac_dest.text.isNullOrBlank()){
                        val macDest = mView.et_mac_dest.text.toString()
                        val colonCount = macDest.length - macDest.replace(":", "").length
                        if(macDest.length == 17 && colonCount == 5) {
                            mView.et_mac_dest.error = null
                            var ethernetDestination = EthernetDestination(mView.et_mac_dest.text.toString())
                            if(ethernetMatch != null){
                                ethernetMatch?.ethernetDestination = ethernetDestination
                            } else {
                                ethernetMatch = EthernetMatch(null, ethernetDestination, null)
                            }
                        } else {
                            mView.et_mac_dest.error = VALUE_MAC_ADDR_ERROR
                            isCompleted = false
                        }
                    } else {
                        mView.et_mac_dest.error = VALUE_CANT_BE_BLANK
                        isCompleted = false
                    }
                }
                tag_match_ipv4source -> {
                    if(!mView.et_ipv4_source.text.isNullOrEmpty() || !mView.et_ipv4_source.text.isNullOrBlank()){
                        val ip = mView.et_ipv4_source.text.toString().trim()
                        if(ip.contains("/")){
                            val ipAddr = ip.substring(0, ip.lastIndexOf("/"))
                            val netmask = ip.substring(ip.lastIndexOf("/")+1)
                            try{
                                if(validIP(ipAddr) && netmask.toInt() in 1..32){
                                    mView.et_ipv4_source.error = null
                                    match.ipv4source = ip
                                } else {
                                    mView.et_ipv4_source.error = VALUE_IP_ADDR_ERROR
                                    isCompleted = false
                                }
                            } catch (e: Exception){
                                mView.et_ipv4_source.error = VALUE_IP_ADDR_ERROR
                                isCompleted = false
                            }
                        } else {
                            mView.et_ipv4_source.error = VALUE_IP_ADDR_ERROR
                            isCompleted = false
                        }
                    } else {
                        mView.et_ipv4_source.error = VALUE_CANT_BE_BLANK
                        isCompleted = false
                    }
                }
                tag_match_ipv4dest -> {
                    if(!mView.et_ipv4_dest.text.isNullOrEmpty() || !mView.et_ipv4_dest.text.isNullOrBlank()){
                        val ip = mView.et_ipv4_dest.text.toString().trim()
                        if(ip.contains("/")){
                            val ipAddr = ip.substring(0, ip.lastIndexOf("/"))
                            val netmask = ip.substring(ip.lastIndexOf("/")+1)
                            try{
                                if(validIP(ipAddr) && netmask.toInt() in 1..32){
                                    mView.et_ipv4_dest.error = null
                                    match.ipv4destination = ip
                                }else {
                                    mView.et_ipv4_dest.error = VALUE_IP_ADDR_ERROR
                                    isCompleted = false
                                }
                            } catch (e: Exception){
                                mView.et_ipv4_dest.error = VALUE_IP_ADDR_ERROR
                                isCompleted = false
                            }

                        } else {
                            mView.et_ipv4_dest.error = VALUE_IP_ADDR_ERROR
                            isCompleted = false
                        }

                    } else {
                        mView.et_ipv4_dest.error = VALUE_CANT_BE_BLANK
                        isCompleted = false
                    }
                }
            }

        }

        match.ethernetMatch = ethernetMatch
        newFlow?.match = match

        if(isCompleted){
            (activity as AddFlowActivity).setDataStatus(true)
        } else {
            (activity as AddFlowActivity).setDataStatus(false)
        }

        return newFlow!!
    }

    fun validIP(ip: String?): Boolean {
        try {
            if (ip == null || ip.isEmpty()) {
                return false
            }

            val parts = ip.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (parts.size != 4) {
                return false
            }

            for (s in parts) {
                val i = Integer.parseInt(s)
                if (i < 0 || i > 255) {
                    return false
                }
            }
            if (ip.endsWith(".")) {
                return false
            }

            return true
        } catch (nfe: NumberFormatException) {
            return false
        }
    }
}