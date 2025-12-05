package top.contins.synapse.domain.service

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.contins.synapse.network.api.ApiService
import top.contins.synapse.network.model.ServiceRoute
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * 路由管理器，负责从服务注册中心加载和管理服务路由信息
 */
@Singleton
class RouteManager @Inject constructor(
    @field:Named("noAuth") private val apiService: ApiService
) {
    private val _routes = MutableStateFlow<Map<String, ServiceRoute>>(emptyMap())
    val routes: StateFlow<Map<String, ServiceRoute>> = _routes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * 从服务注册中心加载路由信息
     * @param serviceRegistryUrl 服务注册中心的URL
     */
    suspend fun loadRoutes(serviceRegistryUrl: String): Boolean {
        return try {
            _isLoading.value = true
            Log.d("RouteManager", "Loading routes from: $serviceRegistryUrl")
            
            val routesUrl = "$serviceRegistryUrl/service-registry/routes"
            val response = apiService.getServiceRoutes(routesUrl)
            
            if (response.code == 0 && response.data != null) {
                val routesData = response.data!!
                _routes.value = routesData
                Log.d("RouteManager", "Successfully loaded ${routesData.size} routes")
                
                // 打印加载的路由信息
                routesData.forEach { (serviceName, route) ->
                    Log.d("RouteManager", "Service: $serviceName -> ${route.getFullEndpoint()}")
                }
                
                true
            } else {
                Log.e("RouteManager", "Failed to load routes: ${response.message}")
                false
            }
        } catch (e: Exception) {
            Log.e("RouteManager", "Error loading routes", e)
            false
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * 获取指定服务的路由信息
     */
    fun getRoute(serviceName: String): ServiceRoute? {
        return _routes.value[serviceName]
    }

    /**
     * 获取指定服务的完整端点URL
     */
    fun getServiceEndpoint(serviceName: String): String? {
        return getRoute(serviceName)?.getFullEndpoint()
    }

    /**
     * 清除所有路由信息
     */
    fun clearRoutes() {
        _routes.value = emptyMap()
        Log.d("RouteManager", "Routes cleared")
    }

    /**
     * 检查是否已加载路由信息
     */
    fun hasRoutes(): Boolean {
        return _routes.value.isNotEmpty()
    }
}