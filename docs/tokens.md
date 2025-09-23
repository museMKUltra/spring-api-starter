# Tokens

## Token Comparisons

| Access Token                     | Refresh Token                  |
|----------------------------------|--------------------------------|
| To access protected endpoint     | To get a new access token      |
| Shore-lived (15m or less)        | Long-lived (7d or more)        |
| Returned in the response body    | Returned as an HttpOnly cookie |
| Stored in memory or LocalStorage | Not accessible via Javascript  |
| LocalStorage is less secure      | Much harder to steal           |

## Logout Strategies

| Client-side Logout                                | Server-side Logout                                              |
|---------------------------------------------------|-----------------------------------------------------------------|
| Delete the access token from memory or storage    | Store a list of active or removed tokens in a database or cache |
| Remove the refresh token (by clearing the cookie) | When a user logs out, mark their token as invalid               |
| Simple to implement                               | During each request, check if the token is blacklisted          |
| Tokens are valid until they expire                | Provides a true logout experience                               |
|                                                   | Add complexity and requires token look up on each request       |
| Best for low-risk scenarios                       | Best for high-security apps                                     |
