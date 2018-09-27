package engine

class BreakoutEngine @JvmOverloads constructor(var canvasWidth: Double,
                                               var canvasHeight: Double,
                                               private var ballRadius: Double = canvasWidth / 40,
                                               val paddleWidth: Double = canvasWidth / 4,
                                               val paddleHeight: Double = canvasHeight / 20,
                                               private val blockColumns: Int = 4,
                                               blockRows: Int = 4,
                                               private val numLives: Int = 5,
                                               private val gameStateListener: GameStateListener) {

    private val initialVelocity = -1.0
    private val blockWidth = canvasWidth / blockColumns
    private val blockHeight = (canvasWidth / 2) / blockRows

    private var ball: Ball = Ball(canvasWidth / 2,
            canvasHeight / 2,
            initialVelocity,
            initialVelocity,
            ballRadius)

    private var paddleX: Double = 0.0
    private var paddleY: Double = canvasHeight - paddleHeight
    private var blockCount = blockColumns * blockRows
    private var blocks = Array(blockCount) { it -> Block((it % blockColumns).toDouble() * blockWidth, (it / blockColumns).toDouble() * blockHeight, blockWidth, blockHeight, BlockState.NEW) }

    var running = true
    private var lives = numLives

    fun resetGame() {
        resetBall()
        resetLives()
        resetBlocks()
    }

    fun resetBall() {
        ball.x = canvasWidth / 2
        ball.y = canvasHeight - paddleHeight - paddleHeight / 2
        ball.velocityX = initialVelocity
        ball.velocityY = initialVelocity
    }

    private fun resetBlocks() {
        for (block in blocks) {
            block.blockState = BlockState.NEW
        }
    }

    private fun resetLives() {
        lives = numLives
        notifyNumLivesChanged()
    }

    private fun notifyNumLivesChanged() {
        gameStateListener.numberOfLivesChanged(lives)
    }

    fun resume() {
        running = true
    }

    fun pause() {
        running = false
    }

    fun step() {
        if (running) {
            gameStateListener.ballMoved(ball.x, ball.y, ballRadius)
            ball.step()
            stepBlocks()
        }
    }

    private fun stepBlocks() {
        for (block in blocks) {
            block.checkHit(ball)
            gameStateListener.blockUpdated(block)
        }
    }

    fun updatePaddleLocation(value: Double) {
        if (running && (value > 0.0 && value < (canvasWidth - paddleWidth))) {
            paddleX = value
            gameStateListener.paddleMoved(value, paddleY, paddleWidth, paddleHeight)
        }
    }

    inner class Ball(ballX: Double,
                     ballY: Double,
                     var velocityX: Double,
                     var velocityY: Double,
                     private val radius: Double) : Rectangle(ballX, ballY, radius * 2, radius * 2) {
        fun step() {
            moveBall()
            checkBallLeftWallBounce()
            checkBallHitOrMissPaddle()
            checkBallTopBounce()
        }

        private fun moveBall() {
            x += velocityX
            y += velocityY
        }

        private fun checkBallLeftWallBounce() {
            if (x >= canvasWidth - radius || x <= radius) {
                bounceHorizontal()
            }
        }

        private fun checkBallHitOrMissPaddle() {
            if (y + velocityY >= canvasHeight - radius - paddleHeight) {
                if (x > paddleX && x < paddleX + paddleWidth) {
                    checkAndSkewBall()
                    bounceVertical()
                } else {
                    handleBallMissedPaddle()
                }
            }
        }

        private fun checkBallTopBounce() {
            if (y <= radius) {
                bounceVertical()
            }
        }

        /**
         * Change the ball x velocity if it hits the left or right edge of the paddle
         */
        private fun checkAndSkewBall() {
            val halfPaddle = paddleWidth / 2
            val centerOfPaddle = paddleX + halfPaddle
            val diff = x - centerOfPaddle
            val percentage = diff / halfPaddle
            if (percentage > 0.5 || percentage < -0.5) {
                velocityX = percentage
            }
        }

        fun bounceHorizontal() {
            velocityX = -velocityX
        }

        fun bounceVertical() {
            velocityY = -velocityY
        }
    }

    private fun handleBallMissedPaddle() {
        lives--
        notifyNumLivesChanged()
        if (lives == 0) {
            pause()
            gameStateListener.gameLose()
        } else {
            gameStateListener.ballMissedPaddle()
        }
    }

    inner class Block(blockX: Double, blockY: Double, blockWidth: Double, blockHeight: Double, var blockState: BlockState) : Rectangle(blockX, blockY, blockWidth, blockHeight) {

        fun checkHit(ball: Ball) {
            if (blockState == BlockState.DESTROYED)
                return

            val ballX = ball.x - ballRadius
            val ballY = ball.y - ballRadius

            if (ballX + ballRadius + ball.velocityX > x
                    && ballX + ball.velocityX < x + w
                    && ballY + ball.h > y
                    && ballY < y + h) {
                ball.bounceHorizontal()
                hit()
            }

            if (ballX + ball.w > x
                    && ballX < x + w
                    && ballY + ball.h + ball.velocityY > y
                    && ballY + ball.velocityY < y + h) {
                ball.bounceVertical()
                hit()
            }
        }

        private fun hit() {
            if (blockState == BlockState.NEW) {
                blockState = BlockState.HIT
            } else if (blockState == BlockState.HIT) {
                blockState = BlockState.DESTROYED
                blockCount--
                if (blockCount == 0) {
                    handleWin()
                }
            }
        }

        override fun toString(): String {
            return "Block(x=$x, y=$y, gameWidth=$w, gameHeight=$h, blockState=$blockState)"
        }
    }

    private fun handleWin() {
        pause()
        gameStateListener.gameWin()
    }

    enum class BlockState {
        NEW,
        HIT,
        DESTROYED
    }
}

open class Rectangle(var x: Double, var y: Double, val w: Double, val h: Double)

interface GameStateListener {
    fun ballMoved(x: Double, y: Double, radius: Double)
    fun paddleMoved(x: Double, y: Double, w: Double, h: Double)
    fun blockUpdated(block: BreakoutEngine.Block)
    fun ballMissedPaddle()
    fun numberOfLivesChanged(lives: Int)
    fun gameLose()
    fun gameWin()
}
