# Tokens

| Access Token                     | Refresh Token                  |
|----------------------------------|--------------------------------|
| To access protected endpoint     | To get a new access token      |
| Shore-lived (15m or less)        | Long-lived (7d or more)        |
| Returned in the response body    | Returned as an HttpOnly cookie |
| Stored in memory or LocalStorage | Not accessible via Javascript  |
| LocalStorage is less secure      | Much harder to steal           |
