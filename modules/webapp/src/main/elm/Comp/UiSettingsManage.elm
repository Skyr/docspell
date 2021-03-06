module Comp.UiSettingsManage exposing
    ( Model
    , Msg(..)
    , init
    , update
    , view
    )

import Api.Model.BasicResult exposing (BasicResult)
import Comp.UiSettingsForm
import Data.Flags exposing (Flags)
import Data.UiSettings exposing (UiSettings)
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)
import Ports


type alias Model =
    { formModel : Comp.UiSettingsForm.Model
    , settings : Maybe UiSettings
    , message : Maybe BasicResult
    }


type Msg
    = UiSettingsFormMsg Comp.UiSettingsForm.Msg
    | Submit
    | SettingsSaved
    | UpdateSettings


init : Flags -> UiSettings -> ( Model, Cmd Msg )
init flags settings =
    let
        ( fm, fc ) =
            Comp.UiSettingsForm.init flags settings
    in
    ( { formModel = fm
      , settings = Nothing
      , message = Nothing
      }
    , Cmd.map UiSettingsFormMsg fc
    )



--- update


update : Flags -> UiSettings -> Msg -> Model -> ( Model, Cmd Msg, Sub Msg )
update flags settings msg model =
    case msg of
        UiSettingsFormMsg lm ->
            let
                ( m_, sett ) =
                    Comp.UiSettingsForm.update settings lm model.formModel
            in
            ( { model
                | formModel = m_
                , settings = sett
                , message =
                    if sett /= Nothing then
                        Nothing

                    else
                        model.message
              }
            , Cmd.none
            , Sub.none
            )

        Submit ->
            case model.settings of
                Just s ->
                    ( { model | message = Nothing }
                    , Ports.storeUiSettings flags s
                    , Ports.onUiSettingsSaved SettingsSaved
                    )

                Nothing ->
                    ( { model | message = Just (BasicResult False "Settings unchanged or invalid.") }
                    , Cmd.none
                    , Sub.none
                    )

        SettingsSaved ->
            ( { model | message = Just (BasicResult True "Settings saved.") }
            , Cmd.none
            , Sub.none
            )

        UpdateSettings ->
            let
                ( fm, fc ) =
                    Comp.UiSettingsForm.init flags settings
            in
            ( { model | formModel = fm }
            , Cmd.map UiSettingsFormMsg fc
            , Sub.none
            )



--- View


isError : Model -> Bool
isError model =
    Maybe.map .success model.message == Just False


isSuccess : Model -> Bool
isSuccess model =
    Maybe.map .success model.message == Just True


view : UiSettings -> String -> Model -> Html Msg
view settings classes model =
    div [ class classes ]
        [ Html.map UiSettingsFormMsg (Comp.UiSettingsForm.view settings model.formModel)
        , div [ class "ui divider" ] []
        , button
            [ class "ui primary button"
            , onClick Submit
            ]
            [ text "Submit"
            ]
        , div
            [ classList
                [ ( "ui message", True )
                , ( "success", isSuccess model )
                , ( "error", isError model )
                , ( "hidden invisible", model.message == Nothing )
                ]
            ]
            [ Maybe.map .message model.message
                |> Maybe.withDefault ""
                |> text
            ]
        ]
