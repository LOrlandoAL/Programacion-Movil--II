import requests

server_key = "TU_SERVER_KEY_DE_FIREBASE"  # desde Firebase > Configuración del proyecto > Cloud Messaging
token = "TOKEN_DEL_DISPOSITIVO"

headers = {
    'Authorization': f'key={server_key}',
    'Content-Type': 'application/json'
}

data = {
    "to": token,
    "notification": {
        "title": "¡Hola desde el servidor!",
        "body": "Este es un mensaje de prueba",
    }
}

response = requests.post("https://fcm.googleapis.com/fcm/send", headers=headers, json=data)
print(response.status_code)
print(response.json())
