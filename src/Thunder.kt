import dev.robocode.tankroyale.botapi.Bot
import dev.robocode.tankroyale.botapi.BotInfo
import dev.robocode.tankroyale.botapi.Color
import dev.robocode.tankroyale.botapi.events.*

fun main() {
    Thunder().start()
}

class Thunder: Bot(BotInfo.fromFile("res/Thunder.json")) {
    private var estrategia: Estrategia = Padrao(this)
    private var estrategia2: Estrategia = Agressiva(this)

    override fun run() {
        tracksColor = Color.fromHex("B0C4DE")
        bodyColor = Color.fromHex("FF4500")
        gunColor = Color.fromHex("869ed6") //8B4513
        turretColor = Color.fromHex("00abff")
        scanColor = Color.fromHex("FF0000")
        estrategia.run()
    }

    override fun onScannedBot(scannedBotEvent: ScannedBotEvent?) {
        estrategia.onScannedBot(scannedBotEvent)
    }

    override fun onHitBot(botHitBotEvent: HitBotEvent?) {
        estrategia.onHitBot(botHitBotEvent)
    }

    override fun onRoundStarted(roundStartedEvent: RoundStartedEvent?) {
        estrategia.onRoundStarted(roundStartedEvent)
    }

    override fun onRoundEnded(roundEndedEvent: RoundEndedEvent?) {
        if (numberOfRounds - 2 == roundNumber) {
            estrategia = estrategia2
        }
    }
}




interface Estrategia {
    fun run()
    fun onHitWall(botHitWallEvent: HitWallEvent?)
    fun onScannedBot(scannedBotEvent: ScannedBotEvent?)
    fun onHitBot(botHitBotEvent: HitBotEvent?)
    fun onRoundStarted(roundStartedEvent: RoundStartedEvent?)
}

class Agressiva(val bot: Bot): Estrategia {
    var bearingToinimigo = 0.0
    var distancia = 0.0
    var turnDirection = 1

    override fun run() {
        while (bot.isRunning) {
            bot.turnLeft((5.0 * turnDirection))
        }
    }

    override fun onHitWall(botHitWallEvent: HitWallEvent?) {

    }

    override fun onScannedBot(scannedBotEvent: ScannedBotEvent?) {
        if (scannedBotEvent != null) {
            turnToFaceTarget(scannedBotEvent.x, scannedBotEvent.y)
            distancia = bot.distanceTo(scannedBotEvent.x, scannedBotEvent.y)
            bot.setForward(distancia + 5)
            toFire(distancia)
        }
    }

    override fun onHitBot(botHitBotEvent: HitBotEvent?) {

    }

    override fun onRoundStarted(roundStartedEvent: RoundStartedEvent?) {
        bearingToinimigo = 0.0
        distancia = 0.0
        turnDirection = 1
    }

    fun turnToFaceTarget(x: Double, y: Double) {
        bearingToinimigo = bot.bearingTo(x, y)
        if (bearingToinimigo >= 0) {
            turnDirection = 1
        } else {
            turnDirection = -1
        }
        bot.turnLeft(bearingToinimigo)
    }

    fun toFire(distancia: Double) {
        if (20 < distancia && 30 < distancia) {
            bot.fire(1.0)
        } else if (distancia < 8) {
            bot.fire(2.0)
        } else if (distancia < 11) {
            bot.fire(3.0)
        }
    }
}

class Padrao(val bot: Bot): Estrategia {
    var turnDirection = 1
    var speedGun = 5.0
    var scannedEnemy = false
    var enemySeen: Int = 0
    var moveDistance = 100.0
    var enemyGrau = 0.0
    var turn = 45.0

    override fun run() {
        while (bot.isRunning) {
            if (!scannedEnemy) {
                bot.turnGunRight(speedGun * turnDirection)

//                changeDirection()
            } else {
                bot.setForward(100.0)
                bot.turnRight(turn)
                time()
                bot.go() // o go() faz o time() depender do onScannedBot(), como, só deus sabe
            }
        }
    }

    override fun onHitWall(botHitWallEvent: HitWallEvent?) {
        moveDistance *= -1
    }

    override fun onScannedBot(scannedBotEvent: ScannedBotEvent?) {
        if (scannedBotEvent != null) {
            scannedEnemy = true
            enemySeen = bot.turnNumber
            enemyGrau = bot.gunBearingTo(scannedBotEvent.x, scannedBotEvent.y)
            correction(bot.gunBearingTo(scannedBotEvent.x, scannedBotEvent.y))
            toFire(bot.distanceTo(scannedBotEvent.x, scannedBotEvent.y), scannedBotEvent.speed)


        }
    }

    override fun onHitBot(botHitBotEvent: HitBotEvent?) {
        if (botHitBotEvent != null) {
            correction(bot.gunBearingTo(botHitBotEvent.x, botHitBotEvent.y))
        }
    }

    override fun onRoundStarted(roundStartedEvent: RoundStartedEvent?) { // evita empilhamento ao trocar de rodada
        scannedEnemy = false
        enemySeen = 0
        enemyGrau = 0.0
    }

    fun correction(angle: Double) {
        if (angle > 5) {
            bot.turnGunRight(angle - turn)
        }
    }

    // Time para sabre se o aquanto tempo não se identifica um inimigo

    fun time() {
        if (scannedEnemy && bot.turnNumber > enemySeen + 10) {
            scannedEnemy = false
            enemySeen = 0
            turnDirection = if (enemyGrau > 0) -1 else 1
        }
    }

    fun toFire(distanceEnemy: Double, speed: Double) {
        if (distanceEnemy < 30 || speed == 0.0) {
            bot.fire(3.0)
        } else if (distanceEnemy < 100) {
            bot.fire(2.0)
        } else if (distanceEnemy < 300) {
            bot.fire(1.0)
        }
    }
}

