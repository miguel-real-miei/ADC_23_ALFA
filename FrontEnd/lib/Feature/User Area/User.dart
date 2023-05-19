class User {
final String id;
final String email;
final String password;
final bool isPublic;
final String? phoneLandline;
final String? phoneMobile;
final String? occupation;
final String? workplace;
final String? address;
final String? complementaryAddress;
final String? location;
final String? postalCode;
final String? nif;
final String? photoUrl;

User({
  required this.id,
  required this.email,
  required this.password,
  required this.isPublic,
  this.phoneLandline,
  this.phoneMobile,
  this.occupation,
  this.workplace,
  this.address,
  this.complementaryAddress,
  this.location,
  this.postalCode,
  this.nif,
  this.photoUrl,
});

// Constructor para criar uma instância de User a partir de um Mapa
factory User.fromJson(Map<String, dynamic> json) {
return User(
id: json['id'],
email: json['email'],
password: json['password'],
isPublic: json['isPublic'],
phoneLandline: json['phoneLandline'],
phoneMobile: json['phoneMobile'],
occupation: json['occupation'],
workplace: json['workplace'],
address: json['address'],
complementaryAddress: json['complementaryAddress'],
location: json['location'],
postalCode: json['postalCode'],
nif: json['nif'],
photoUrl: json['photoUrl'],
);
}
}


