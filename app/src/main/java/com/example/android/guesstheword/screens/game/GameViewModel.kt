package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar

class GameViewModel : ViewModel() {

    companion object {
        //Time when the game is over.
        private const val DONE = 0L

        //countdown time interval
        private const val ONE_SECOND = 1000L

        //tOTAL TIME FOR THE GAME
        private const val COUNTDOWN_TIME = 15000L
    }


    //PRÁCTICA: determina el estado del cronómetro: En pausa o corriendo.
    private val _chronoState = MutableLiveData<Boolean>()
    val chronoState: LiveData<Boolean>
    get() = _chronoState

    //PRÁCTICA: cooldown entre uso del cronómetro por parte del usuario, para evitar que se acelere la cuenta atrás
    private var timerCooldown : CountDownTimer?

    //store the current time - countdown
    private val _currentTime = MutableLiveData<Long>()
    private val currentTime: LiveData<Long>
        get() = _currentTime

    //Timer
    private var timer: CountDownTimer

    //current time in MM:SS
    val currentTimeString = Transformations.map(currentTime) { time ->
        DateUtils.formatElapsedTime(time)
    }

    //detecta si esta en los ultimos segundos para poner el tiempo en rojo.
    private val _finalSeconds = MutableLiveData<Boolean>()
    val finalSeconds: LiveData<Boolean>
        get() = _finalSeconds

    // The current word
    private val _word = MutableLiveData<String>()
    val word : LiveData<String>
        get() = _word

    //Transformation para Hint
    val hintWord = Transformations.map(word) { word ->
        val pos=(1..word.length-1).random()
        "Current word has ${word.length} letters\nThe letter at position ${pos+1} is ${word[pos]}"
    }

    // The current score
    private val _score = MutableLiveData<Int>()
    val score : LiveData<Int>
        get() = _score

    private val _eventGameFinish = MutableLiveData<Boolean>()
    val eventGameFinish: LiveData<Boolean>
        get() = _eventGameFinish

    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>

    //creación del objeto
    init {
        //PRÁCTICA: Se inicializa el cronómetro con el valor principal
        timer = newTimer(COUNTDOWN_TIME)
        timer.start()
        timerCooldown = null
        //PRÁCTICA: Se inicializa el estado del cronómetro a activo
        _chronoState.value = true
        _word.value = ""
        _score.value = 0
        _eventGameFinish.value = false
        _finalSeconds.value = false
        Log.i("GameViewModel","GameViewModel creado")
        resetList()
        nextWord()
    }

    //método en la destrucción del objeto.
    override fun onCleared() {
        super.onCleared()
        Log.i("GameViewModel", "GameViewModel destruido!")
        timer.cancel()
        //PRÁCTICA:
        timerCooldown?.cancel()
    }

    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
            "queen",
            "hospital",
            "basketball",
            "cat",
            "change",
            "snail",
            "soup",
            "calendar",
            "sad",
            "desk",
            "guitar",
            "home",
            "railway",
            "zebra",
            "jelly",
            "car",
            "crow",
            "trade",
            "bag",
            "roll",
            "bubble"
        )
        wordList.shuffle()
    }

    /**
     * Moves to the next word in the list
     */
    //toma la siguiente palabra, si está vacía vuelve a resetear la lista.
    private fun nextWord() {
        if (!wordList.isEmpty()) {
            //Select and remove a word from the list
            _word.value = wordList.removeAt(0)
        } else {
            resetList()
        }
    }
    //decrementa los puntos
    fun onSkip() {
        _score.value = (score.value)?.minus(1)
        nextWord()
    }
    //aumenta los puntos.
    fun onCorrect() {
        _score.value = (score.value)?.plus(1)
        nextWord()
    }
    //se activa cuando el contador llega a cero.
    fun onGameFinish() {
        _eventGameFinish.value = true
    }
    //baja la bandera tras lanzar el método.
    fun onGameFinishComplete() {
        _eventGameFinish.value = false
    }
    //se activa cuando quedan menos de 10 segundos - en el temporizador.
    fun onFinalSeconds() {
        _finalSeconds.value = true
    }
    //baja la bandera tras cambiar el color del texto.
    fun onFinalSecondsComplete() {
        _finalSeconds.value = false
    }

    //PRÁCTICA: cambia el estado del cronómetro
    /**
     * Alterna el estado del cronómetro, pero solo si este no se encuentra en cooldown, y ejecuta
     * los métodos necesarios para que la funcionalidad del cronómetro se ejecute acorde al estado
     * en que acaba.
     */
    fun toggleChronoState() {
        //si el cambio de estado del cronómetro está en cooldown, entonces no hace nada
        if (timerCooldown != null) {
            return
        }
        //almacena temporalmente el estado actual del cronómetro invertido
        val currentState : Boolean = !requireNotNull(chronoState.value)
        //actualiza el valor del estado del cronómetro
        _chronoState.value = currentState
        //si el cronómetro está activo, entonces reanuda la cuenta atrás, si no, entonces lo para
        if (currentState)
            resumeChronoCount()
        else
            stopChronoCount()
        //inicia el cooldown del cambio de estado del contador
        runCooldown()
    }

    //PRÁCTICA
    /**
     * Crea una cuenta atrás de un segundo y la almacena en la variable timerCooldown.
     *
     * Cuando la cuenta atrás finaliza o se interrumpe, anula la cuenta atrás existente, poniéndola a null.
     */
    private fun runCooldown () {
        timerCooldown = object : CountDownTimer(ONE_SECOND, ONE_SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                //no es necesario hacer nada en este método.
                Log.d(":::CHRONO_CD", "onTick().")
            }

            override fun onFinish() {
                //solamente se elimina la referencia al objeto, de tal manera que se limpia la memoria
                //y puede usarse su propia existencia o no existencia como bandera
                Log.d(":::CHRONO_CD", "onFinish(). Destruyendo el timerCooldown")
                timerCooldown = null
            }//se inicializa el contador
        }.start()
    }

    //PRÁCTICA
    /** Interrumpe la cuenta atrás actual del cronómetro de juego. */
    private fun stopChronoCount() {
        Log.d(":::CHRONO", "Parando el cronómetro. Valor del contador: ${_currentTime.value}")
        timer.cancel()
    }

    //PRÁCTICA
    /**
     * Reinicia la cuenta atrás del contador desde el estado donde la dejó.
     *
     * Este método crea un nuevo cronómetro que parte desde el momento donde lo dejó el anterior y llama a su método start().
     */
    private fun resumeChronoCount() {
        timer = newTimer(requireNotNull(_currentTime.value) * ONE_SECOND)
        timer.start()
        Log.d(":::CHRONO", "Iniciando el cronómetro. Valor del contador: ${_currentTime.value}")
    }

    //PRÁCTICA
    /**
     * Devuelve un [CountDownTimer] de acuerdo a los requisitos del cronómetro principal de la partida que empieza a contar
     * desde el momento determinado por [countdownTime].
     *
     * @param countdownTime Tiempo desde el que se comienza a contar.
     */
    private fun newTimer(countdownTime: Long) : CountDownTimer {
       return object : CountDownTimer(countdownTime, ONE_SECOND) {
           override fun onTick(millisUntilFinished: Long) {
               _currentTime.value = millisUntilFinished / ONE_SECOND
               Log.d(":::CHRONO", "onTick(). Valor del contador: ${_currentTime.value}")
               if (_currentTime.value == 10L) onFinalSeconds()
           }

           override fun onFinish() {
               //PRÁCTICA: se modifica el método onFinish() anterior, puesto que establecer el valor del tiempo a cero
               //al eliminar el CountDownTimer rompe completamente la lógica aplicada para su funcionalidad de parada
               //y continuación.
               Log.d(":::CHRONO", "Destruyendo el cronómetro. Valor del contador: ${_currentTime.value}")
               //Se llama al método onGameFinish() solo si el contador ya tiene un valor igual o menor (para solventar casos de error)
               //al de DONE.
               if ( (_currentTime.value as Long) <= DONE) onGameFinish()
           }
       }
    }
}