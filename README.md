################################################################
#                                                              #
#                      SEATING-SERVICE                         #
#                                                              #
################################################################


{
  "topicName": "SeatRequest",
  "correlatorId": 123456,
  "movieName": "The Dark Knight",
  "showtime": "2025-11-10T21:45:00-06:00",
  "seatNumber": "A5"
}

SELECT id, movie_name, genre, price, jsonb_pretty(seats) AS seats_pretty
FROM movie_service.movies;