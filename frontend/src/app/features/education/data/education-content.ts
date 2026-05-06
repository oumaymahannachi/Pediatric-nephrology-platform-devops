import { Article, Video, QuizQuestion, DailyTip, EducationCategory } from '../models/education.model';

export const ARTICLES: Article[] = [
  {
    id: '1',
    title: 'Syndrome néphrotique',
    simplifiedTitle: 'Fuite de protéines dans les urines',
    category: EducationCategory.KIDNEY_DISEASE,
    icon: '🫘',
    tags: ['reins', 'protéines', 'œdèmes'],
    readTime: 3,
    content: `
      <h3>🫘 C'est quoi le syndrome néphrotique ?</h3>
      <p>Les reins de votre enfant laissent passer des protéines importantes dans les urines, alors qu'ils devraient les garder dans le sang.</p>
      
      <div class="info-box">
        <strong>🔍 Signes à surveiller :</strong>
        <ul>
          <li>Gonflement du visage (surtout le matin)</li>
          <li>Gonflement des pieds et des jambes</li>
          <li>Urines mousseuses</li>
          <li>Prise de poids rapide</li>
        </ul>
      </div>
      
      <h3>💊 Le traitement</h3>
      <p>Le médecin prescrit généralement de la <strong>cortisone</strong> (prednisolone). Il est très important de ne jamais arrêter ce médicament sans avis médical.</p>
      
      <div class="warning-box">
        <strong>⚠️ Quand appeler le médecin ?</strong>
        <ul>
          <li>Fièvre supérieure à 38.5°C</li>
          <li>Gonflement qui augmente rapidement</li>
          <li>Votre enfant ne peut plus uriner</li>
        </ul>
      </div>
    `
  },
  {
    id: '2',
    title: 'Insuffisance rénale chronique',
    simplifiedTitle: 'Les reins ne fonctionnent pas à 100%',
    category: EducationCategory.KIDNEY_DISEASE,
    icon: '🔬',
    tags: ['reins', 'eGFR', 'dialyse'],
    readTime: 4,
    content: `
      <h3>🔬 Comprendre l'insuffisance rénale</h3>
      <p>Les reins filtrent normalement 100% du sang. Dans l'insuffisance rénale, cette capacité est réduite. On mesure cela avec l'<strong>eGFR</strong>.</p>
      
      <div class="stages-box">
        <strong>📊 Les stades :</strong>
        <div class="stage stage-1">Stade 1-2 : eGFR > 60 → Léger, surveillance</div>
        <div class="stage stage-3">Stade 3 : eGFR 30-60 → Modéré, traitement</div>
        <div class="stage stage-4">Stade 4 : eGFR 15-30 → Sévère, préparation</div>
        <div class="stage stage-5">Stade 5 : eGFR < 15 → Dialyse nécessaire</div>
      </div>
      
      <h3>🥗 Alimentation importante</h3>
      <p>Limitez le sel, le potassium (bananes, oranges) et le phosphore (produits laitiers en excès).</p>
    `
  },
  {
    id: '3',
    title: 'Comment donner les médicaments',
    simplifiedTitle: 'Guide pratique pour les médicaments',
    category: EducationCategory.MEDICATION,
    icon: '💊',
    tags: ['médicaments', 'cortisone', 'dosage'],
    readTime: 2,
    content: `
      <h3>💊 Règles d'or pour les médicaments</h3>
      
      <div class="tip-list">
        <div class="tip-item">✅ Toujours donner à la même heure</div>
        <div class="tip-item">✅ Avec de la nourriture pour la cortisone</div>
        <div class="tip-item">✅ Ne jamais doubler la dose si oubli</div>
        <div class="tip-item">❌ Ne jamais arrêter sans avis médical</div>
        <div class="tip-item">❌ Ne pas écraser les comprimés sans demander</div>
      </div>
      
      <h3>📅 Si vous oubliez une dose</h3>
      <p>Si vous vous souvenez dans les 2 heures → donnez la dose. Sinon → attendez la prochaine dose normale.</p>
      
      <div class="warning-box">
        <strong>⚠️ Effets secondaires de la cortisone à surveiller :</strong>
        <ul>
          <li>Prise de poids</li>
          <li>Humeur changeante</li>
          <li>Appétit augmenté</li>
        </ul>
        <p>Ces effets sont normaux et diminuent avec le temps.</p>
      </div>
    `
  },
  {
    id: '4',
    title: 'Alimentation adaptée',
    simplifiedTitle: 'Ce que votre enfant peut manger',
    category: EducationCategory.NUTRITION,
    icon: '🥗',
    tags: ['alimentation', 'sel', 'potassium'],
    readTime: 3,
    content: `
      <h3>🥗 Alimentation pour les maladies rénales</h3>
      
      <div class="food-grid">
        <div class="food-good">
          <h4>✅ À privilégier</h4>
          <ul>
            <li>🍚 Riz, pâtes, pain blanc</li>
            <li>🥕 Carottes, haricots verts</li>
            <li>🍎 Pommes, poires</li>
            <li>💧 Eau (selon les conseils du médecin)</li>
          </ul>
        </div>
        <div class="food-bad">
          <h4>⚠️ À limiter</h4>
          <ul>
            <li>🧂 Sel et aliments salés</li>
            <li>🍌 Bananes, oranges (potassium)</li>
            <li>🥛 Produits laitiers en excès</li>
            <li>🥩 Viande rouge en grande quantité</li>
          </ul>
        </div>
      </div>
      
      <h3>💡 Astuce pratique</h3>
      <p>Faites tremper les légumes dans l'eau 2h avant cuisson pour réduire le potassium.</p>
    `
  },
  {
    id: '5',
    title: 'Surveiller la tension artérielle',
    simplifiedTitle: 'Comment mesurer et comprendre la tension',
    category: EducationCategory.MONITORING,
    icon: '🩺',
    tags: ['tension', 'surveillance', 'hypertension'],
    readTime: 2,
    content: `
      <h3>🩺 Pourquoi surveiller la tension ?</h3>
      <p>Une tension élevée peut aggraver les maladies rénales. Il est important de la mesurer régulièrement.</p>
      
      <div class="info-box">
        <strong>📏 Valeurs normales pour un enfant :</strong>
        <p>Cela dépend de l'âge et de la taille. Votre médecin vous donnera les valeurs cibles.</p>
      </div>
      
      <h3>📋 Comment bien mesurer</h3>
      <div class="tip-list">
        <div class="tip-item">1️⃣ Votre enfant doit être calme depuis 5 minutes</div>
        <div class="tip-item">2️⃣ Assis, bras au niveau du cœur</div>
        <div class="tip-item">3️⃣ Mesurer 2 fois, noter les deux valeurs</div>
        <div class="tip-item">4️⃣ Toujours au même moment de la journée</div>
      </div>
    `
  },
  {
    id: '6',
    title: 'Signes d\'urgence',
    simplifiedTitle: 'Quand aller aux urgences ?',
    category: EducationCategory.EMERGENCY,
    icon: '🚨',
    tags: ['urgence', 'danger', 'hôpital'],
    readTime: 2,
    content: `
      <h3>🚨 Allez aux urgences immédiatement si :</h3>
      
      <div class="emergency-list">
        <div class="emergency-item">🔴 Votre enfant ne peut plus uriner depuis 8h</div>
        <div class="emergency-item">🔴 Gonflement du visage très important</div>
        <div class="emergency-item">🔴 Difficultés à respirer</div>
        <div class="emergency-item">🔴 Convulsions</div>
        <div class="emergency-item">🔴 Perte de conscience</div>
      </div>
      
      <h3>📞 Appelez le médecin si :</h3>
      <div class="warning-list">
        <div class="warning-item">🟡 Fièvre > 38.5°C</div>
        <div class="warning-item">🟡 Urines très foncées ou rouges</div>
        <div class="warning-item">🟡 Douleurs abdominales fortes</div>
        <div class="warning-item">🟡 Vomissements répétés</div>
      </div>
    `
  }
];

export const VIDEOS: Video[] = [
  {
    id: 'v1',
    title: 'Comment donner un médicament à un enfant',
    youtubeId: 'dQw4w9WgXcQ',
    duration: '2:30',
    category: EducationCategory.MEDICATION,
    thumbnail: '💊',
    description: 'Guide pratique pour administrer les médicaments correctement'
  },
  {
    id: 'v2',
    title: 'Comprendre les reins en 2 minutes',
    youtubeId: 'dQw4w9WgXcQ',
    duration: '1:45',
    category: EducationCategory.KIDNEY_DISEASE,
    thumbnail: '🫘',
    description: 'Explication simple du fonctionnement des reins'
  },
  {
    id: 'v3',
    title: 'Alimentation adaptée aux maladies rénales',
    youtubeId: 'dQw4w9WgXcQ',
    duration: '3:00',
    category: EducationCategory.NUTRITION,
    thumbnail: '🥗',
    description: 'Quoi manger et quoi éviter pour protéger les reins'
  },
  {
    id: 'v4',
    title: 'Mesurer la tension artérielle à la maison',
    youtubeId: 'dQw4w9WgXcQ',
    duration: '2:15',
    category: EducationCategory.MONITORING,
    thumbnail: '🩺',
    description: 'Technique correcte pour mesurer la tension'
  }
];

export const QUIZ_QUESTIONS: QuizQuestion[] = [
  {
    id: 'q1',
    question: 'Que faire si vous oubliez de donner un médicament ?',
    options: [
      'Doubler la dose suivante',
      'Donner la dose si moins de 2h, sinon attendre la prochaine',
      'Arrêter le traitement',
      'Donner immédiatement peu importe l\'heure'
    ],
    correctIndex: 1,
    explanation: 'Si vous vous souvenez dans les 2 heures, donnez la dose. Sinon, attendez la prochaine dose normale. Ne doublez jamais la dose.',
    category: EducationCategory.MEDICATION
  },
  {
    id: 'q2',
    question: 'Quel aliment est à limiter pour un enfant avec une maladie rénale ?',
    options: ['Riz blanc', 'Pommes', 'Bananes', 'Pain blanc'],
    correctIndex: 2,
    explanation: 'Les bananes sont riches en potassium, qui peut s\'accumuler dans le sang quand les reins ne fonctionnent pas bien.',
    category: EducationCategory.NUTRITION
  },
  {
    id: 'q3',
    question: 'Que signifie un eGFR de 25 ?',
    options: [
      'Les reins fonctionnent normalement',
      'Légère réduction de la fonction rénale',
      'Insuffisance rénale sévère (stade 4)',
      'Dialyse immédiatement nécessaire'
    ],
    correctIndex: 2,
    explanation: 'Un eGFR entre 15 et 30 correspond au stade 4 (sévère). Le médecin prépare les options de traitement comme la dialyse.',
    category: EducationCategory.KIDNEY_DISEASE
  },
  {
    id: 'q4',
    question: 'Quand faut-il aller aux urgences ?',
    options: [
      'Légère fièvre à 37.5°C',
      'Votre enfant ne peut pas uriner depuis 8 heures',
      'Légère fatigue',
      'Appétit réduit'
    ],
    correctIndex: 1,
    explanation: 'L\'absence d\'urine pendant 8 heures est une urgence médicale. Les reins peuvent être en danger.',
    category: EducationCategory.EMERGENCY
  },
  {
    id: 'q5',
    question: 'Comment réduire le potassium dans les légumes ?',
    options: [
      'Les cuire à la vapeur',
      'Les manger crus',
      'Les faire tremper dans l\'eau 2h avant cuisson',
      'Les congeler'
    ],
    correctIndex: 2,
    explanation: 'Faire tremper les légumes dans l\'eau pendant 2 heures avant la cuisson permet de réduire significativement leur teneur en potassium.',
    category: EducationCategory.NUTRITION
  },
  {
    id: 'q6',
    question: 'Quel est un signe du syndrome néphrotique ?',
    options: [
      'Urines très foncées',
      'Urines mousseuses et gonflement du visage',
      'Douleurs dans le dos',
      'Fièvre élevée'
    ],
    correctIndex: 1,
    explanation: 'Le syndrome néphrotique se manifeste par des urines mousseuses (protéines) et des gonflements, surtout du visage le matin.',
    category: EducationCategory.KIDNEY_DISEASE
  }
];

export const DAILY_TIPS: DailyTip[] = [
  { icon: '🧂', title: 'Limitez le sel', message: 'Évitez d\'ajouter du sel dans les plats de votre enfant aujourd\'hui. Utilisez des herbes aromatiques à la place.', category: 'nutrition' },
  { icon: '💧', title: 'Hydratation', message: 'Encouragez votre enfant à boire de l\'eau régulièrement. Évitez les sodas et jus sucrés.', category: 'nutrition' },
  { icon: '💊', title: 'Médicaments', message: 'N\'oubliez pas de donner les médicaments à la même heure chaque jour pour une meilleure efficacité.', category: 'medication' },
  { icon: '⚖️', title: 'Pesée quotidienne', message: 'Pesez votre enfant ce matin. Une prise de poids rapide peut indiquer une rétention d\'eau.', category: 'monitoring' },
  { icon: '🩺', title: 'Tension artérielle', message: 'Pensez à mesurer la tension de votre enfant aujourd\'hui et notez-la dans son carnet de suivi.', category: 'monitoring' },
  { icon: '🏃', title: 'Activité physique', message: 'Une activité physique légère est bénéfique. Évitez les sports de contact et les efforts intenses.', category: 'lifestyle' },
  { icon: '😴', title: 'Sommeil', message: 'Assurez-vous que votre enfant dort suffisamment. Le repos aide les reins à récupérer.', category: 'lifestyle' },
  { icon: '🥗', title: 'Légumes du jour', message: 'Pensez à faire tremper les légumes 2h avant cuisson pour réduire le potassium.', category: 'nutrition' },
  { icon: '📋', title: 'Carnet de suivi', message: 'Notez les symptômes d\'aujourd\'hui dans le carnet de suivi pour le prochain rendez-vous médical.', category: 'monitoring' },
  { icon: '🌡️', title: 'Température', message: 'Si votre enfant a de la fièvre > 38.5°C, contactez le médecin rapidement.', category: 'monitoring' },
  { icon: '🍌', title: 'Potassium', message: 'Évitez les bananes, oranges et tomates aujourd\'hui. Préférez les pommes et les poires.', category: 'nutrition' },
  { icon: '💪', title: 'Encouragement', message: 'Rappelez à votre enfant qu\'il est courageux. Votre soutien est sa meilleure médecine !', category: 'lifestyle' },
  { icon: '🏥', title: 'Rendez-vous', message: 'Vérifiez si votre prochain rendez-vous médical est bien noté dans votre agenda.', category: 'monitoring' },
  { icon: '🧴', title: 'Hygiène', message: 'Lavez-vous les mains avant de donner les médicaments pour éviter les infections.', category: 'medication' }
];
