import 'package:flutter/material.dart';

import '../../Core/Animation/Fade_Animation.dart';
import '../../Core/Colors/Hex_Color.dart';
import '../Login Screen/Login_Screen.dart';

enum FormData { name, phone, email, uniNumber, role, password, confirmPassword }

class SignupScreen extends StatefulWidget {
  const SignupScreen({super.key});

  @override
  State<SignupScreen> createState() => _SignupScreenState();
}

class _SignupScreenState extends State<SignupScreen> {
  Color enabled = const Color.fromARGB(255, 63, 56, 89);
  Color enabledTxt = Colors.white;
  Color unselected = Colors.grey;
  Color backgroundColor = const Color(0xFF1F1A30);
  bool isPasswordEv = true;

  FormData? selected;

  TextEditingController nameController = TextEditingController();
  TextEditingController phoneController = TextEditingController();
  TextEditingController emailController = TextEditingController();
  TextEditingController uniNumberController = TextEditingController();
  TextEditingController roleController = TextEditingController();
  TextEditingController passwordController = TextEditingController();
  TextEditingController confirmPasswordController = TextEditingController();

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
                            "https://cdni.iconscout.com/illustration/premium/thumb/job-starting-date-2537382-2146478.png",
                            width: 100,
                            height: 100,
                          ),
                        ),
                        const SizedBox(
                          height: 10,
                        ),
                        FadeAnimation(
                          delay: 1,
                          child: Text(
                            "Criar conta",
                            style: TextStyle(
                                color: Colors.white.withOpacity(0.9),
                                letterSpacing: 0.5),
                          ),
                        ),
                        //nome
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
                              color: selected == FormData.name
                                  ? enabled
                                  : backgroundColor,
                            ),
                            padding: const EdgeInsets.all(5.0),
                            child: TextField(
                              controller: nameController,
                              onTap: () {
                                setState(() {
                                  selected = FormData.name;
                                });
                              },
                              decoration: InputDecoration(
                                enabledBorder: InputBorder.none,
                                border: InputBorder.none,
                                prefixIcon: Icon(
                                  Icons.title,
                                  color: selected == FormData.name
                                      ? enabledTxt
                                      : unselected,
                                  size: 20,
                                ),
                                hintText: 'Nome Completo',
                                hintStyle: TextStyle(
                                    color: selected == FormData.name
                                        ? enabledTxt
                                        : unselected,
                                    fontSize: 12),
                              ),
                              textAlignVertical: TextAlignVertical.center,
                              style: TextStyle(
                                  color: selected == FormData.name
                                      ? enabledTxt
                                      : unselected,
                                  fontWeight: FontWeight.bold,
                                  fontSize: 12),
                            ),
                          ),
                        ),
                        //tlm
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
                              color: selected == FormData.phone
                                  ? enabled
                                  : backgroundColor,
                            ),
                            padding: const EdgeInsets.all(5.0),
                            child: TextField(
                              controller: phoneController,
                              onTap: () {
                                setState(() {
                                  selected = FormData.phone;
                                });
                              },
                              decoration: InputDecoration(
                                enabledBorder: InputBorder.none,
                                border: InputBorder.none,
                                prefixIcon: Icon(
                                  Icons.phone_android_rounded,
                                  color: selected == FormData.phone
                                      ? enabledTxt
                                      : unselected,
                                  size: 20,
                                ),
                                hintText: 'Numero Telemovel',
                                hintStyle: TextStyle(
                                    color: selected == FormData.phone
                                        ? enabledTxt
                                        : unselected,
                                    fontSize: 12),
                              ),
                              textAlignVertical: TextAlignVertical.center,
                              style: TextStyle(
                                  color: selected == FormData.phone
                                      ? enabledTxt
                                      : unselected,
                                  fontWeight: FontWeight.bold,
                                  fontSize: 12),
                            ),
                          ),
                        ),
                        //email
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
                        //numero institucional
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
                                color: selected == FormData.uniNumber
                                    ? enabled
                                    : backgroundColor),
                            padding: const EdgeInsets.all(5.0),
                            child: TextField(
                              controller: uniNumberController,
                              onTap: () {
                                setState(() {
                                  selected = FormData.uniNumber;
                                });
                              },
                              decoration: InputDecoration(
                                  enabledBorder: InputBorder.none,
                                  border: InputBorder.none,
                                  prefixIcon: Icon(
                                    Icons.account_balance,
                                    color: selected == FormData.uniNumber
                                        ? enabledTxt
                                        : unselected,
                                    size: 20,
                                  ),
                                  hintText: 'Numero Institucional',
                                  hintStyle: TextStyle(
                                      color: selected == FormData.uniNumber
                                          ? enabledTxt
                                          : unselected,
                                      fontSize: 12)),
                              textAlignVertical: TextAlignVertical.center,
                              style: TextStyle(
                                  color: selected == FormData.uniNumber
                                      ? enabledTxt
                                      : unselected,
                                  fontWeight: FontWeight.bold,
                                  fontSize: 12),
                            ),
                          ),
                        ),
                        //role
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
                                color: selected == FormData.uniNumber
                                    ? enabled
                                    : backgroundColor),
                            padding: const EdgeInsets.all(5.0),
                            child: DropdownButtonFormField(
                              value: 'Aluno',
                              items: _rolesList
                                  .map((e) => DropdownMenuItem(
                                  child: Text(e), value: e))
                                  .toList(),
                              onChanged: (val) {
                                setState(() {
                                  selected = FormData.role;
                                });
                              },
                              icon: const Icon(Icons.arrow_drop_down,
                                  color: Colors.white70),
                              dropdownColor: backgroundColor,
                              decoration: InputDecoration(
                                enabledBorder: InputBorder.none,
                                border: InputBorder.none,
                                prefixIcon: Icon(
                                  Icons.account_box_rounded,
                                  color: selected == FormData.role
                                      ? enabledTxt
                                      : unselected,
                                  size: 20,
                                ),
                              ),
                            ),
                          ),
                        ),
                        //pwd
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
                        //confirmar pwd
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
                                color: selected == FormData.confirmPassword
                                    ? enabled
                                    : backgroundColor),
                            padding: const EdgeInsets.all(5.0),
                            child: TextField(
                              controller: confirmPasswordController,
                              onTap: () {
                                setState(() {
                                  selected = FormData.confirmPassword;
                                });
                              },
                              decoration: InputDecoration(
                                  enabledBorder: InputBorder.none,
                                  border: InputBorder.none,
                                  prefixIcon: Icon(
                                    Icons.lock_open_outlined,
                                    color: selected == FormData.confirmPassword
                                        ? enabledTxt
                                        : unselected,
                                    size: 20,
                                  ),
                                  suffixIcon: IconButton(
                                    icon: isPasswordEv
                                        ? Icon(
                                      Icons.visibility_off,
                                      color: selected ==
                                          FormData.confirmPassword
                                          ? enabledTxt
                                          : unselected,
                                      size: 20,
                                    )
                                        : Icon(
                                      Icons.visibility,
                                      color: selected ==
                                          FormData.confirmPassword
                                          ? enabledTxt
                                          : unselected,
                                      size: 20,
                                    ),
                                    onPressed: () => setState(
                                            () => isPasswordEv = !isPasswordEv),
                                  ),
                                  hintText: 'Confirmar Password',
                                  hintStyle: TextStyle(
                                      color:
                                      selected == FormData.confirmPassword
                                          ? enabledTxt
                                          : unselected,
                                      fontSize: 12)),
                              obscureText: isPasswordEv,
                              textAlignVertical: TextAlignVertical.center,
                              style: TextStyle(
                                  color: selected == FormData.confirmPassword
                                      ? enabledTxt
                                      : unselected,
                                  fontWeight: FontWeight.bold,
                                  fontSize: 12),
                            ),
                          ),
                        ),
                        const SizedBox(
                          height: 25,
                        ),
                        FadeAnimation(
                          delay: 1,
                          child: TextButton(
                              onPressed: () {},
                              style: TextButton.styleFrom(
                                  backgroundColor: const Color(0xFF2697FF),
                                  padding: const EdgeInsets.symmetric(
                                      vertical: 14.0, horizontal: 80),
                                  shape: RoundedRectangleBorder(
                                      borderRadius:
                                      BorderRadius.circular(12.0))),
                              child: const Text(
                                "Registar",
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
                  height: 20,
                ),

                FadeAnimation(
                  delay: 1,
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Text("Já tens conta?",
                          style: TextStyle(
                            color: Colors.grey,
                            letterSpacing: 0.5,
                          )),
                      GestureDetector(
                        onTap: () {
                          Navigator.pop(context);
                          Navigator.of(context)
                              .push(MaterialPageRoute(builder: (context) {
                            return const LoginScreen();
                          }));
                        },
                        child: Text("  Faz login",
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

final _rolesList = ['Aluno', 'Docente', 'Funcionário'];
