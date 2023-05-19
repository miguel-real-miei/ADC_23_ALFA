

import 'package:flutter/material.dart';
import 'User.dart';

class UserProfileScreen extends StatelessWidget {
  final User user;

  const UserProfileScreen({super.key, required this.user});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Perfil do Usuário'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            if (user.photoUrl != null)
              Image.network(user.photoUrl!),
            Text('ID: ${user.id}'),
            Text('Email: ${user.email}'),
            Text('Perfil: ${user.isPublic ? 'Público' : 'Privado'}'),
            if (user.phoneLandline != null)
              Text('Telefone Fixo: ${user.phoneLandline}'),
            if (user.phoneMobile != null)
              Text('Telefone Móvel: ${user.phoneMobile}'),
            if (user.occupation != null)
              Text('Ocupação: ${user.occupation}'),
            if (user.workplace != null)
              Text('Local de trabalho: ${user.workplace}'),
            if (user.address != null)
              Text('Morada: ${user.address}'),
            if (user.complementaryAddress != null)
              Text('Morada Complementar: ${user.complementaryAddress}'),
            if (user.location != null)
              Text('Localidade: ${user.location}'),
            if (user.postalCode != null)
              Text('CP: ${user.postalCode}'),
            if (user.nif != null)
              Text('NIF: ${user.nif}'),
          ],
        ),
      ),
    );
  }
}