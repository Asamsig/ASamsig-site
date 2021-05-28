package asamsig.calculator

import asamsig.Main
import asamsig.calculator.Calculation.{CalculationInput, CalculationProps}
import org.scalajs.dom
import org.scalajs.dom.{Event, html}
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.{ReactElement, SetStateHookCallback}
import slinky.core.{FunctionalComponent, SyntheticEvent}
import slinky.web.html._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.util.Try

@react object CalculatorPage {

  val component = FunctionalComponent[Unit] { _ =>
    useEffect(() => {
      dom.window.scrollTo(0, 0)
    })

    div(className := "calculator-content")(
      Calculation.component(CalculationProps(CalculationInput("3", Option("What is "), Option("% ")), CalculationInput("5", Option(" of "), Option("?")), (percentage, amount) => amount / 100 * percentage)),
      Calculation.component(CalculationProps(CalculationInput("5", None, Option("is what percent")), CalculationInput("5", Option(" of "), Option("?")), (percentage, amount) => percentage / amount * 100, Some("%"))),
      Calculation.component(CalculationProps(CalculationInput("5", Option("What is the percentage increase/decrease from "), Option("to ")), CalculationInput("5", None, Option(" ?")), (a, c) => (c - a) / a * 100, Some("%")))
    )
  }
}

@react object Calculation {

  case class CalculationInput(inputSize: String,
                              inputPreText: Option[String] = None,
                              inputPostText: Option[String] = None
                             )

  case class CalculationProps(inputOne: CalculationInput, inputTwo: CalculationInput, calculation: (BigDecimal, BigDecimal) => BigDecimal, resultIndicator: Option[String] = None)

  val component = FunctionalComponent[CalculationProps] { props =>
    val (inputOne, setInputOne) = useState[Option[BigDecimal]](None)
    val (inputTwo, setInputTwo) = useState[Option[BigDecimal]](None)
    val (result, setResult) = useState[Option[BigDecimal]](None)

    useEffect(() => {
      setResult(
        for {
          one <- inputOne
          two <- inputTwo
          result <- Try(props.calculation(one, two)).toOption
        } yield {
          result
        }
      )
    }, Seq(inputOne, inputTwo))

    CalculationForm.component(
      fieldset(style := literal(border = "none", fontSize = "20px"))(
        inputCalc(inputOne, setInputOne, props.inputOne.inputSize, props.inputOne.inputPreText, props.inputOne.inputPostText),
        inputCalc(inputTwo, setInputTwo, props.inputTwo.inputSize, props.inputTwo.inputPreText, props.inputTwo.inputPostText),
        inputCalc(result, setResult, "6", Option("Result: "), props.resultIndicator, inputStyle = "result", inputReadOnly = true)
      )
    )
  }

  def handleChange(hook: SetStateHookCallback[Option[BigDecimal]])(e: SyntheticEvent[html.Input, Event]): Unit = {
    val value1 = Try(BigDecimal(e.target.value)).toOption
    hook(value1)
  }

  def inputCalc(inputValue: Option[BigDecimal],
                setInputValue: SetStateHookCallback[Option[BigDecimal]],
                inputSize: String,
                inputPreText: Option[String],
                inputPostText: Option[String] = None,
                inputStyle: String = "values",
                inputReadOnly: Boolean = false
               ) = {
    span(className := inputStyle)(
      inputPreText.mkString,
      input(
        style := literal(fontSize = "18px", width = s"${inputSize}em"),
        `type` := "number",
        readOnly := inputReadOnly,
        onChange := (handleChange(setInputValue)(_)),
        value := inputValue.mkString
      ),
      span(style := literal(visibility = inputPostText.fold("hidden")(_ => "visible"), marginRight = inputPostText.fold("20px")(_ => "0")))(
        s" ${inputPostText.mkString}"
      )
    )
  }
}

@react object CalculationForm {
  val component = FunctionalComponent[ReactElement] { props =>
    Main.darkModeContext.Consumer(darkMode =>
      div(style := literal(
        padding = "10px",
      ))(
        div(style := literal(
          color = darkMode.color,
          backgroundColor = darkMode.backgroundColor,
          padding = "20px",
          borderRadius = "10px",
          border = s"1px solid ${darkMode.color}",
          width = "auto",
          margin = "10px 0"
        ))(
          props
        )
      )
    )
  }
}