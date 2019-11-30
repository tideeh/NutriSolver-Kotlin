package br.com.nutrisolver.utils

import java.util.*

// DB constants
const val DB_TIMEOUT_TO_REQUEST = 1 * 60 * 1000
const val DB_COLLECTION_INGREDIENTES = "ingredientes"
const val DB_COLLECTION_DIETAS = "dietas"
const val DB_COLLECTION_LOTES = "lotes"
const val DB_COLLECTION_FAZENDAS = "fazendas"

// sharedPreferences nome
const val SP_NOME = "nutrisolver_prefs"

// sharedPreferences put/get
const val SP_KEY_INGREDIENTES_NOMES = "ingredientes_nomes"
const val SP_KEY_FAZENDA_CORRENTE_ID = "fazenda_corrente_id"
const val SP_KEY_FAZENDA_CORRENTE_NOME = "fazenda_corrente_nome"
const val SP_KEY_INGREDIENTES_NOMES_LAST_UPDATE = "ingredientes_nomes_last_update"
const val SP_KEY_DEVICE_MAC_ADDRESS = "device_mac_address"

// defaults values (quando nao encontra o valor pedido)
const val DEFAULT_STRING_VALUE = "-1"
const val DEFAULT_LONG_VALUE : Long = 0

// intent put/get extras
const val INTENT_KEY_LOTE_SELECIONADO_ID = "lote_selecionado_id"
const val INTENT_KEY_LOTE_SELECIONADO_NOME = "lote_selecionado_nome"
const val INTENT_KEY_DIETA_CADASTRADA = "dieta_cadastrada"
const val INTENT_KEY_LOTE_CADASTRADO = "lote_cadastrado"
const val INTENT_KEY_DEVICE_MAC_ADDRESS = "device_mac_address"

// bundle put/get extras
const val BUNDLE_KEY_TOAST = "toast"
const val BUNDLE_KEY_LOTES_FRAGMENT = "lotesFragment"
const val BUNDLE_KEY_DIETAS_FRAGMENT = "dietasFragment"
const val BUNDLE_KEY_TESTES_FRAGMENT = "testesFragment"
const val BUNDLE_KEY_LISTA_DIETAS = "lista_dietas"
const val BUNDLE_KEY_FROM_ON_SAVE_INSTANCE_STATE_DIETAS = "from_onSaveInstanceState_dietas"
const val BUNDLE_KEY_LISTA_LOTES = "lista_lotes"
const val BUNDLE_KEY_FROM_ON_SAVE_INSTANCE_STATE_LOTES = "from_onSaveInstanceState_lotes"

const val STRING_LIST_DELIMITER = ";;;"

// sendData constants (comunicacao entre os fragments e o activity do tablayout)
const val SEND_DATA_FRAGMENT_DIETAS = "DietasFragment"
const val SEND_DATA_FRAGMENT_LOTES = "LotesFragment"
const val SEND_DATA_FRAGMENT_TESTES = "TestesFragment"
const val SEND_DATA_COMMAND_ADICIONA_DIETA = "adiciona_dieta"
const val SEND_DATA_COMMAND_ADICIONA_LOTE = "adiciona_lote"
const val SEND_DATA_COMMAND_ATUALIZA_LOTES = "atualiza_lotes"
const val SEND_DATA_COMMAND_ATUALIZA_DIETAS = "atualiza_dietas"

// messages (mensagens enviadas para as activitys handleMessage)
const val MESSAGE_WHAT_READ = 0
const val MESSAGE_WHAT_WRITE = 1
const val MESSAGE_WHAT_TOAST = 2

//  Bluetooth UUID
const val BT_MODULE_UUID_STRING = "00001101-0000-1000-8000-00805F9B34FB"

// Bluetooth comannds
const val BT_COMMAND_DESLIGAR_LED = "0"
const val BT_COMMAND_LIGAR_LED = "1"
const val BT_COMMAND_LIGAR_BUZZER = "2"
const val BT_COMMAND_DESLIGAR_BUZZER = "3"

// startActivityForResult requests
const val ACTIVITY_REQUEST_CONECTAR_DISPOSITIVO_BT = 1001
const val ACTIVITY_REQUEST_LIGAR_BT = 1002
const val ACTIVITY_REQUEST_LOGIN_COM_GOOGLE = 1003
const val ACTIVITY_REQUEST_REGISTRAR = 1004
const val ACTIVITY_REQUEST_CADASTRAR_FAZENDA = 1005
const val ACTIVITY_REQUEST_CADASTRAR_DIETA = 1006

// requests keys de permissions
const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 2001