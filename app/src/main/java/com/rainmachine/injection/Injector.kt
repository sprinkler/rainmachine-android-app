package com.rainmachine.injection

import com.rainmachine.data.local.database.model.Device
import com.rainmachine.domain.boundary.data.PrefRepository
import com.rainmachine.infrastructure.SprinklerManager
import com.rainmachine.infrastructure.util.RainApplication
import dagger.ObjectGraph

object Injector {

    private lateinit var app: RainApplication
    lateinit var graph: ObjectGraph
    @JvmStatic
    var sprinklerGraph: ObjectGraph? = null
    private lateinit var prefRepository: PrefRepository

    @JvmStatic
    fun initAndInjectApp(app: RainApplication) {
        this.app = app
        createGraphAndInjectApp()
    }

    @JvmStatic
    fun createGraphAndInjectApp() {
        createGraph()
        inject(app)
        prefRepository = graph.get(PrefRepository::class.java)
    }

    private fun createGraph() {
        graph = ObjectGraph.create(*Modules.list(app))
    }

    @JvmStatic
    fun <T> inject(instance: T): T = graph.inject(instance)

    @JvmStatic
    fun buildSprinklerGraph(device: Device) {
        removeSprinklerGraph()

        sprinklerGraph = graph.plus(SprinklerModule(device))

        val sprinklerManager = sprinklerGraph?.get(SprinklerManager::class.java)
        sprinklerManager?.init()

        prefRepository.saveCurrentDeviceId(device.deviceId)
        prefRepository.saveCurrentDeviceType(device.type)
    }

    @JvmStatic
    fun removeSprinklerGraph() {
        if (sprinklerGraph != null) {
            val sprinklerManager = sprinklerGraph?.get(SprinklerManager::class.java)
            sprinklerManager?.cleanUp()
        }
        sprinklerGraph = null

        prefRepository.clearCurrentDeviceId()
        prefRepository.clearCurrentDeviceType()
    }

    @JvmStatic
    fun <T> injectSprinklerGraph(instance: T): T = sprinklerGraph!!.inject(instance)
}