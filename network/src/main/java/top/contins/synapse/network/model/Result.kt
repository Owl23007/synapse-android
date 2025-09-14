package top.contins.synapse.network.model

class Result<T> {
    var code: Int? =  null
    var message: String? = null
    var data: T? = null

    constructor()

    constructor(code:  Int, message: String?, data: T?) {
        this.code = code
        this.message = message
        this.data = data
    }
}