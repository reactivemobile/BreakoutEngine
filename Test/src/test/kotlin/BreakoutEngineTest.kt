import engine.BreakoutEngine

internal class BreakoutEngineTest {

    @org.junit.jupiter.api.Test
    fun testGameWin() {
        var winCalled = false
        lateinit var gameEngine: BreakoutEngine

        val listener = object : engine.GameStateListener {
            override fun ballMoved(x: Double, y: Double, radius: Double) {
            }

            override fun paddleMoved(x: Double, y: Double, w: Double, h: Double) {
            }

            override fun blockUpdated(block: engine.BreakoutEngine.Block) {
            }

            override fun ballMissedPaddle() {
                gameEngine.resetBall()
            }

            override fun numberOfLivesChanged(lives: Int) {
            }

            override fun gameLose() {

            }

            override fun gameWin() {
                winCalled = true
            }
        }

        gameEngine = engine.BreakoutEngine(10.0,
                10.0,
                5.0,
                10.0,
                4.0,
                4,
                4, 5, listener)

        repeat(500) { gameEngine.step() }
        assert(winCalled)
    }

    @org.junit.jupiter.api.Test
    fun testGameLose() {
        var loseCalled = false
        lateinit var gameEngine: engine.BreakoutEngine

        val listener = object : engine.GameStateListener {
            override fun ballMoved(x: Double, y: Double, radius: Double) {
            }

            override fun paddleMoved(x: Double, y: Double, w: Double, h: Double) {
            }

            override fun blockUpdated(block: engine.BreakoutEngine.Block) {
            }

            override fun ballMissedPaddle() {
                gameEngine.resetBall()
            }

            override fun numberOfLivesChanged(lives: Int) {
            }

            override fun gameLose() {
                loseCalled = true
            }

            override fun gameWin() {
            }
        }

        gameEngine = engine.BreakoutEngine(10.0,
                10.0,
                2.0,
                1.0,
                1.0,
                4,
                4, 5, listener)

        repeat(500) { gameEngine.step() }
        assert(loseCalled)
    }
}