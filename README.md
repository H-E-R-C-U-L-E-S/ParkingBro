#👋 👋 👋 Hello 👋 👋 👋

## App Idea
ParkingBro allows users to search by phone number for the owner and his car number 🤫 😮 😮
Or to search for the owner and phone number by the car number, for this the user needs a few things 😏 🤓 😎

## Rules and abilities
🧐 🧐 🧐
1. Must pass authentication 🥱🥱🥱
2. To search for the car number, he must add his car number (the car number is attached to the user number) 💩💩💩  
!!! The user will not be able to add the car if this number is already linked to someone else (if he deletes it, he can) 🤠👻😁  
3. When searching by phone number, the user must give us access to read contacts (automatically uploaded to the database)😮‍💨😮‍💨😮‍💨  

Activities and fragments
The first activity in the application is AuthActivity, in it we use two fragments for authentication and verification 🤡  
The second active is MainActivity in which we have navigation (meaning navhostfragment and BottomNavigationView) 😀 😃 😄  
In searchFragment, we have a viewPager with which we move between selection modes (with car number - phone number) 😵‍💫 🫥 🤐  

💀💀💀
If we have a search number in the database, then a BottomDialogFragment will appear with the user, from which the user can

1. Call the number 😎 😦 😧 😮  
2. Copy (number only)  
3. Save in saves (using SharedPreferences + Gson (to represent the object with json and save it)) 🥵 🥵 🥵  
4. Repair (full information) 😳 😳  

😈 👿 👹
Saved items are shown in recyclerview and can be deleted by dragging the item to the left  

## profile
On the profile, you can see how the user's name is written in the database (it can be changed because it looks like someone else has written it (then it will appear with this house))  
The user can:  
1. delete the machine 🥲 🥹 🥲 🥹  
2. Add e-mail (it will not appear anywhere yet)  

The data is stored in FirebaseDatabase and we authenticate the user with firebaseAuthentication  

## Animations
!!! For animations, we use gifs and load them into imageView, lottie animations could not be output, these animations had watermarks 😭😭😭😭😭😭  

## Demo Link
--- See the application video at the link 👉 👉 👉 https://drive.google.com/file/d/1fw2V1W-ZCLGcmqBHtWu8xv6m9NYR4nlE/view?usp=sharing  
## P.s
The emulator works, all these functions work in the application 🤩🥳 However, the design of the save and profile needs a little improvement 👏👏👏 🤪🤪🤪 🤑🤑🤑
