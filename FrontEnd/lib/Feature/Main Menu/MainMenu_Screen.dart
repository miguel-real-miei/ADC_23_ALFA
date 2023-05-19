import 'package:flutter/material.dart';
import 'package:http/http.dart';
import '../../Core/Animation/Fade_Animation.dart';
import '../../Core/Colors/Hex_Color.dart';
import '../Forgot Password/Forgot_Password_Screen.dart';
import '../Sign Up Screen/SignUp_Screen.dart';
import 'dart:convert';

import '../User Area/User.dart';
import '../User Area/UserProfileScreen.dart';

enum FormData {
  email,
  password,
}

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  Color enabled = const Color.fromARGB(255, 63, 56, 89);
  Color enabledTxt = Colors.white;
  Color unselected = Colors.grey;
  Color backgroundColor = const Color(0xFF1F1A30);
  bool isPasswordEv = true;
  FormData? selected;

  TextEditingController emailController = TextEditingController();
  TextEditingController passwordController = TextEditingController();


  void login(String email, password) async {
    try {
      Response response = await post(Uri.parse('url para dar post do login'),
          body: {'email': email, 'password': password});
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body.toString());
        print(data['token']);
        print('account logged in successfully');

        // Criando um objeto User com base na resposta
        User user = User.fromJson(data['user']);

        // Navegando para UserProfileScreen após login bem-sucedido
        // ignore: use_build_context_synchronously
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => UserProfileScreen(user: user),
          ),
        );
      } else {
        print('Failed login');
      }//
    } catch (e) {
      print(e.toString());
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            stops: const [0.1, 0.4, 0.7, 0.9],
            colors: [
              HexColor("#4b4293").withOpacity(0.8),
              HexColor("#4b4293"),
              HexColor("#08418e"),
              HexColor("#08418e")
            ],
          ),
          image: DecorationImage(
            fit: BoxFit.cover,
            colorFilter: ColorFilter.mode(
                HexColor("#fff").withOpacity(0.2), BlendMode.dstATop),
            image: const NetworkImage(
              'https://mir-s3-cdn-cf.behance.net/project_modules/fs/01b4bd84253993.5d56acc35e143.jpg',
            ),
          ),
        ),
        child: Center(
          child: SingleChildScrollView(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Card(
                  elevation: 5,
                  color:
                  const Color.fromARGB(255, 171, 211, 250).withOpacity(0.4),
                  child: Container(
                    width: 400,
                    padding: const EdgeInsets.all(40.0),
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        FadeAnimation(
                          delay: 0.8,
                          child: Image.network(
                            "https://www.fct.unl.pt/sites/default/files/images/nova_4.png",
                            width: 100,
                            height: 100,
                          ),
                        ),
                        const SizedBox(
                          height: 10,
                        ),
                        const FadeAnimation(
                          delay: 1,
                          child: Text(
                            "Faz login",
                            style: TextStyle(
                                color: Colors.white, letterSpacing: 0.5),
                          ),
                        ),
                        const SizedBox(
                          height: 20,
                        ),
                        FadeAnimation(
                          delay: 1,
                          child: Container(
                            width: 300,
                            height: 40,
                            decoration: BoxDecoration(
                              borderRadius: BorderRadius.circular(12.0),
                              color: selected == FormData.email
                                  ? enabled
                                  : backgroundColor,
                            ),
                            padding: const EdgeInsets.all(5.0),
                            child: TextField(
                              controller: emailController,
                              onTap: () {
                                setState(() {
                                  selected = FormData.email;
                                });
                              },
                              decoration: InputDecoration(
                                enabledBorder: InputBorder.none,
                                border: InputBorder.none,
                                prefixIcon: Icon(
                                  Icons.email_outlined,
                                  color: selected == FormData.email
                                      ? enabledTxt
                                      : unselected,
                                  size: 20,
                                ),
                                hintText: 'Email',
                                hintStyle: TextStyle(
                                    color: selected == FormData.email
                                        ? enabledTxt
                                        : unselected,
                                    fontSize: 12),
                              ),
                              textAlignVertical: TextAlignVertical.center,
                              style: TextStyle(
                                  color: selected == FormData.email
                                      ? enabledTxt
                                      : unselected,
                                  fontWeight: FontWeight.bold,
                                  fontSize: 12),
                            ),
                          ),
                        ),
                        const SizedBox(
                          height: 20,
                        ),
                        FadeAnimation(
                          delay: 1,
                          child: Container(
                            width: 300,
                            height: 40,
                            decoration: BoxDecoration(
                                borderRadius: BorderRadius.circular(12.0),
                                color: selected == FormData.password
                                    ? enabled
                                    : backgroundColor),
                            padding: const EdgeInsets.all(5.0),
                            child: TextField(
                              controller: passwordController,
                              onTap: () {
                                setState(() {
                                  selected = FormData.password;
                                });
                              },
                              decoration: InputDecoration(
                                  enabledBorder: InputBorder.none,
                                  border: InputBorder.none,
                                  prefixIcon: Icon(
                                    Icons.lock_open_outlined,
                                    color: selected == FormData.password
                                        ? enabledTxt
                                        : unselected,
                                    size: 20,
                                  ),
                                  suffixIcon: IconButton(
                                    icon: isPasswordEv
                                        ? Icon(
                                      Icons.visibility_off,
                                      color: selected == FormData.password
                                          ? enabledTxt
                                          : unselected,
                                      size: 20,
                                    )
                                        : Icon(
                                      Icons.visibility,
                                      color: selected == FormData.password
                                          ? enabledTxt
                                          : unselected,
                                      size: 20,
                                    ),
                                    onPressed: () => setState(
                                            () => isPasswordEv = !isPasswordEv),
                                  ),
                                  hintText: 'Password',
                                  hintStyle: TextStyle(
                                      color: selected == FormData.password
                                          ? enabledTxt
                                          : unselected,
                                      fontSize: 12)),
                              obscureText: isPasswordEv,
                              textAlignVertical: TextAlignVertical.center,
                              style: TextStyle(
                                  color: selected == FormData.password
                                      ? enabledTxt
                                      : unselected,
                                  fontWeight: FontWeight.bold,
                                  fontSize: 12),
                            ),
                          ),
                        ),
                        const SizedBox(
                          height: 20,
                        ),
                        FadeAnimation(
                          delay: 1,
                          child: TextButton(
                              onPressed: () {
                                // Navigator.pop(context);
                                // Navigator.of(context)
                                //     .push(MaterialPageRoute(builder: (context) {
                                //   return MyApp(isLogin: true);
                                // }));
                              },
                              style: TextButton.styleFrom(
                                  backgroundColor: const Color(0xFF2697FF),
                                  padding: const EdgeInsets.symmetric(
                                      vertical: 14.0, horizontal: 80),
                                  shape: RoundedRectangleBorder(
                                      borderRadius:
                                      BorderRadius.circular(12.0))),
                              child: const Text(
                                "Login",
                                style: TextStyle(
                                  color: Colors.white,
                                  letterSpacing: 0.5,
                                  fontSize: 16.0,
                                  fontWeight: FontWeight.bold,
                                ),
                              )),
                        ),
                      ],
                    ),
                  ),
                ),

                //End of Center Card
                //Start of outer card
                const SizedBox(
                  height: 10,
                ),
                FadeAnimation(
                  delay: 1,
                  child: GestureDetector(
                    onTap: (() {
                      Navigator.pop(context);
                      Navigator.of(context)
                          .push(MaterialPageRoute(builder: (context) {
                        return const ForgotPasswordScreen();
                      }));
                    }),
                    child: Text("Esqueceu-se da password?",
                        style: TextStyle(
                          color: Colors.white.withOpacity(0.9),
                          letterSpacing: 0.5,
                        )),
                  ),
                ),
                const SizedBox(height: 10),
                FadeAnimation(
                  delay: 1,
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Text("Não tens conta? ",
                          style: TextStyle(
                            color: Colors.grey,
                            letterSpacing: 0.5,
                          )),
                      GestureDetector(
                        onTap: () {
                          Navigator.pop(context);
                          Navigator.of(context)
                              .push(MaterialPageRoute(builder: (context) {
                            return const SignupScreen();
                          }));
                        },
                        child: Text("Regista-te",
                            style: TextStyle(
                                color: Colors.white.withOpacity(0.9),
                                fontWeight: FontWeight.bold,
                                letterSpacing: 0.5,
                                fontSize: 14)),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
