# Movie-Database
This project is a movie database website and android application, with primary focus on the backend engineering. 

## Demo
Here's a link to a video demo of the project. I've also attatched screenshots and explanations of the demo below. <br>
Link: https://youtu.be/mHMexpRrwj4

### Initial Page
When visiting the website, the user must log in and fill out the reCAPTCHA before they are able to access any content. 
<br><br>
![login_incomplete](https://user-images.githubusercontent.com/52221230/127397755-e5cab1e5-b834-462f-a56d-4757e549594f.JPG)
![login_complete](https://user-images.githubusercontent.com/52221230/127397581-2a4d8f68-e182-4261-be1a-93e71d4a2dd4.JPG)

### Main Page
After logging in, the user is directed to the main page. From the main page, the user has 8 combinations to sort their movies by. 
They can choose to sort by rating first or title first in ascending or descending order. The user can choose the amount of movies per page, filter
by the genre, and view more movies on the next page. 
<br><br>
![front-page](https://user-images.githubusercontent.com/52221230/127397907-c930e811-bc7e-424b-8e4f-6f2599f48f9b.JPG)
Next/Previous page (grayed out if there's no next page or if there's no previous page)
![next_page](https://user-images.githubusercontent.com/52221230/127398572-5e07c197-3966-41ed-9936-66386f73026d.JPG)

### Search
When the user searches for a movie, the autocomplete feature might help them find their movie. If they select a movie from the
autocomplete feature, they'll directly visit the single movie page. Otherwise, a page of suggested movies will appear.
<br><br>
![autocomplete](https://user-images.githubusercontent.com/52221230/127398820-3e44e6d9-ab6f-47ad-85a6-23dcd46a40a8.JPG)

### Single Movie Page
From the single movie page, the user can view details of the movie, such as the director, cast, and ratings. The user may also
add the movie to their shopping cart.
<br><br>
![single-movie](https://user-images.githubusercontent.com/52221230/127399026-7ae8cc54-026e-4d3b-839e-ace35f7173b9.JPG)


### Single Star Page
The single star page displays every movie that the star is in. The user can click a movie to visit the respective single movie page.
<br><br>
![single-star](https://user-images.githubusercontent.com/52221230/127399179-ee4d9ad2-f4b2-43b2-94fb-d8a7c2aafd63.JPG)

### Shopping Cart
If the user clicks checkout, they can view the movies they have in their shopping cart, then proceed to the payment page.
<br><br>
![cart_page](https://user-images.githubusercontent.com/52221230/127399516-95b88b84-d9a3-40f7-8fa5-628f8d6e44d9.JPG)


### Payment Page
At the payment page, the user must enter valid credentials stored in the database to checkout. 
<br><br>
![payment](https://user-images.githubusercontent.com/52221230/127399692-d836abd5-3672-40cc-80b0-8022e3987aec.JPG)
![payment_invalid](https://user-images.githubusercontent.com/52221230/127399753-8b121cfe-8137-481a-93a7-97c0121c7342.JPG)


### Confirmation Page
After paying, the user can view their confirmation page.
<br><br>
![payment_complete](https://user-images.githubusercontent.com/52221230/127399826-31d1b462-b075-4ee4-a7d2-9fb34bee64b1.JPG)

### Android Login
On the android device, the user must log in before they can view content.
<br><br>
![android_loggedin](https://user-images.githubusercontent.com/52221230/127399894-00953bc3-31fd-4624-9dc3-d21b6792fa83.JPG)

### Android Search
The user can search for their desired content
<br><br>
![android_search](https://user-images.githubusercontent.com/52221230/127399978-afa96885-1479-479e-9213-bc7a35fe3e87.JPG)

### Android MovieList
A list of movie suggestions is provided after search. 
<br><br>
![android_movielist](https://user-images.githubusercontent.com/52221230/127400088-c358b8be-b0aa-4a00-8af4-0233d6d47309.JPG)

