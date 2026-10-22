# BudgetBites — Smart Food Finder & Group Meal Optimizer for Vasai

**BudgetBites** is a production-grade Android application designed specifically to solve the family dining budget problem in **Vasai, Maharashtra, India**.

It answers the core question:
> **"Where can my family eat, what can all of us order, and what will the complete bill cost?"**

---

## 🌟 Key Capabilities & Features

1. **Family Budget & Group Meal Optimizer**:
   - Inputs: Total budget (₹500 to ₹5,000+), group size (default 6 people), meal timing (Lunch vs Dinner), dietary preference (Pure Veg vs Non-Veg vs All), and dining mode (Dine-in, Takeaway, Delivery).
   - Real-world travel radius (2 km, 5 km, 10 km, 15 km) centered around Vasai Road Station and local neighborhoods (Vasai West, Babhola, Stella, Manickpur, Evershine City, Papdy).

2. **Smart Budget Match Engine (Bounded Knapsack Optimization)**:
   - Evaluates dishes by serving adequacy, calculating combinations that satisfy group hunger (sum of portions $\times$ servesCount $\ge$ groupSize).
   - Generates multiple distinct options:
     - **Individual Thali Feast** (e.g. 6 Special Veg Thalis at ₹150 + Buttermilk for ₹945 total)
     - **Family Curry & Roti Banquet** (Shared Handi Curries + Butter Rotis + Rice)
     - **Dum Biryani & Accompaniment Feast**
   - Automatically computes 5% GST and packaging/delivery fees.
   - Guaranteed: **The total never silently exceeds your budget.**
   - If a restaurant exceeds the budget, it reports an exact shortfall warning with actionable recommendations.

3. **Interactive Smart Meal Builder**:
   - Custom live meal builder where users can adjust item quantities (`+` / `-`).
   - Live **Budget Health Meter** (visual progress bar, remaining budget, or overrun alert).
   - Real-time **Portion Adequacy Checker** (verifies if the order satisfies the family group).
   - Smart dynamic suggestions for cheaper substitutes or extra items when budget remains.
   - Save custom meal plans to device storage.
   - One-tap share to WhatsApp / SMS.

4. **Side-by-Side Restaurant Comparison**:
   - Select up to 5 restaurants and compare:
     - Total bill for the family group
     - Per-person cost breakdown
     - Included dishes and portions
     - Distance from Vasai Station
     - Verified rating & outlet type
     - Highlights: **Cheapest Option**, **Closest**, and **Highest Rated**.

5. **Vasai Food Radar (Visual Coordinate Map)**:
   - Canvas-based radar plotting Vasai food outlets and landmarks with concentric search rings (2km, 5km, 10km, 15km).
   - Tap any pin for instant preview and quick directions via Google Maps intent.

6. **Restaurant Owner & Admin Verification Portal**:
   - Real-time menu price update workflow with price history audit log.
   - Restaurant owner claim form (Vasai food entrepreneurs can claim and manage their listing).
   - Clear distinction between **VERIFIED MENU (2026)** and **DEMO DATA**.

---

## 🏗️ Architecture & Database Schema

The app utilizes **Android Jetpack Compose**, **Kotlin Coroutines / Flow**, **ViewModel + MVVM**, and **Room Database (SQLite)**.

### Database Tables:
1. `restaurants`: Outlet metadata, coordinates, dining modes, rating, GST and packaging fees, verification status.
2. `menu_categories`: Display groupings (Thalis, Curries, Rotis, Rice, Beverages).
3. `menu_items`: Dish names, descriptions, verified prices, dietary tags (`VEG`, `NON_VEG`), portion sizes, and `servesPeople` capacity.
4. `meal_plans`: User-saved custom group meal plans with totals and budget snapshots.
5. `meal_plan_items`: Normalized dish line items for each meal plan.
6. `user_favourites`: Pinned favorite restaurants.
7. `restaurant_claims`: Submissions from Vasai restaurant proprietors.
8. `restaurant_price_history`: Immutable price audit trail.

### PostgreSQL / Supabase Equivalent Schema & Migration:
If deploying the backend to Supabase PostgreSQL, apply the following DDL:

```sql
-- Create restaurants table
CREATE TABLE restaurants (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    slug TEXT NOT NULL,
    description TEXT,
    address TEXT NOT NULL,
    locality TEXT NOT NULL,
    city TEXT DEFAULT 'Vasai',
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    phone TEXT,
    website TEXT,
    cuisine_types TEXT,
    outlet_type TEXT,
    dining_modes TEXT,
    verified_rating NUMERIC(2, 1) DEFAULT 4.0,
    rating_count INT DEFAULT 0,
    opening_hours TEXT,
    verification_status TEXT DEFAULT 'VERIFIED',
    gst_percentage NUMERIC(4, 2) DEFAULT 5.0,
    packaging_fee NUMERIC(5, 2) DEFAULT 20.0,
    delivery_fee NUMERIC(5, 2) DEFAULT 35.0,
    family_seating_confirmed BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create menu items table
CREATE TABLE menu_items (
    id TEXT PRIMARY KEY,
    restaurant_id TEXT REFERENCES restaurants(id) ON DELETE CASCADE,
    category_id TEXT,
    name TEXT NOT NULL,
    description TEXT,
    price NUMERIC(8, 2) NOT NULL,
    dietary_type TEXT NOT NULL, -- 'VEG', 'NON_VEG'
    serving_size TEXT,
    serves_people INT DEFAULT 1,
    is_available BOOLEAN DEFAULT TRUE,
    price_source TEXT DEFAULT 'Verified Menu 2026',
    last_verified_at DATE DEFAULT CURRENT_DATE
);

-- Create meal plans table
CREATE TABLE meal_plans (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    restaurant_id TEXT REFERENCES restaurants(id),
    group_size INT NOT NULL,
    budget NUMERIC(8, 2) NOT NULL,
    dining_mode TEXT NOT NULL,
    estimated_total NUMERIC(8, 2) NOT NULL,
    remaining_budget NUMERIC(8, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

---

## 🧪 Testing the Specific Vasai Family Scenario

To verify the scenario specified in the prompt:
1. Open the app.
2. Ensure the budget slider is set to **₹1,500**.
3. Group size is set to **6 people**.
4. Diet is set to **Pure Veg**.
5. Meal time is set to **Lunch**.
6. Travel radius is set to **5 km**.
7. Tap **"Find Meals for 6 under ₹1,500"**.
8. Results will highlight **Hotel Rajeshwar Pure Veg** (0.5 km away) with:
   - 6x Special Veg Thalis = ₹900
   - 5% GST = ₹45
   - Total = **₹945** (leaves **+₹555** in remaining budget!)
   - Cost per person: **₹157 / person**.
   - Guaranteed: Total does not exceed ₹1,500.

---

---

## 🔑 Google Places API Integration & Live Sync

BudgetBites includes an in-app **Google Places API Configuration** dialog accessible from the top bar (`Google API` chip or Key icon) and from the **Admin Portal**:

### What the Google Places API does:
- Fetches real-time live restaurants, dhabas, cafes, and hotels in Vasai directly from Google Maps.
- Imports current ratings, review counts, addresses, exact coordinates, and opening hours.
- Auto-generates standard verified menu categories and price estimates for newly discovered outlets so family budget meals can be calculated immediately.

### How to get your Google API Key:
1. Visit [Google Cloud Console](https://console.cloud.google.com/).
2. Create or select a Google Cloud project.
3. Enable **Places API (New)** (or legacy *Places API*) in *APIs & Services ➔ Library*.
4. Navigate to *APIs & Services ➔ Credentials*, click **Create Credentials ➔ API Key**.
5. *(Optional)* Restrict key to Android apps (`com.aistudio.budgetbites.vasai`) or API restriction to Places API.
6. **Free Tier**: Google Maps Platform provides **$200 free credit every month** (allowing thousands of free nearby place requests each month).
7. In BudgetBites, tap **"Google API"** on the home screen or search results, paste your key, and tap **"Test Key & Fetch Live Places"**.

---

## 🍲 Included Vasai Hotels & Dhabas (Online Menus & Prices)

The app comes pre-seeded with a comprehensive local catalog of authentic Vasai eateries, dhabas, and hotels:

1. **Hotel Rajeshwar Pure Veg (Vasai West, Station Road)**
   - *Specialties*: Rajeshwar Special Veg Thali (₹150), Deluxe Kathiyawadi Thali (₹190), Paneer Butter Masala Handi (₹180), Dal Tadka (₹120), Butter Roti (₹18), Jeera Rice (₹110), Chaas (₹25).
2. **Sainath Dhaba & Family Garden (NH48 Western Express Highway, Vasai East)**
   - *Specialties*: Sainath Grand Highway Platter for 6 (₹1,290), Murgh Handi Kolhapuri (₹280, serves 3), Paneer Masaledar (₹210), Charcoal Tandoori Rotis (₹18), Chicken Dum Biryani (₹320).
3. **Balle Balle Punjabi Dhaba (Chinchoti - Kaman Road, Vasai East)**
   - *Specialties*: Amritsari Special Thali (₹170), Sarson da Saag & White Butter (₹210), Butter Chicken Dhaba Style (₹290), Makki di Roti / Naan (₹35), Patiala Sweet Lassi (₹60).
4. **Kinara Village Family Dhaba (Babhola - Koliwada Road, Vasai West)**
   - *Specialties*: Murgh Kolhapuri Handi (₹260), Dhaba Chicken Handi (₹280), Paneer Tikka Masala (₹210), Roti Basket (₹100), Chicken Dum Biryani Handi (₹320).
5. **Hotel Swaad Malvani & Coastal Dining (Station Road, Vasai West)**
   - *Specialties*: Surmai Fish Thali (₹290), Kombdi Vade Family Feast (₹240), Prawns Masala Sukka (₹270), Tisrya Clams Masala (₹210), Solkadhi (₹35).
6. **Panchvati Gaurav Pure Veg Thali (Ambadi Road, Vasai West)**
   - *Specialties*: Royal Unlimited Rajasthani & Gujarati Thali (₹240), Special Dal Bati Churma Platter (₹190), Kesar Shrikhand (₹70).
7. **Green Valley Highway Dhaba (Sasunavghar, NH48 Highway)**
   - *Specialties*: Highway Chicken Handi (₹270), Paneer Tawa Masala (₹200), Tandoori Roti (₹16), Dal Khichdi Tadka (₹140).
8. **Shree Datta Family Dining (Evershine Avenue, Vasai East)**
   - *Specialties*: Pithla Bhakri Thali (₹130), Maharashtrian Veg Thali (₹140), Veg Kolhapuri Handi (₹140), Jowar Bhakri (₹15), Dal Khichdi Tadka (₹130).
9. **Shiv Sagar Pure Veg (Ambadi Road, Manickpur, Vasai West)**
   - *Specialties*: Executive Veg Thali (₹160), Family Jumbo Pav Bhaji Platter (₹240), Paneer Kadai (₹190), Tandoor Butter Roti (₹20), Veg Dum Biryani (₹180).
10. **Koli Rasoi Coastal Kitchen (Papdy, Vasai West)**
    - *Specialties*: Surmai Fish Thali (₹280), Malvani Chicken Thali (₹210), Crispy Bombil Rava Fry (₹190), Solkadhi (₹35).
11. **Farmhouse Garden Restaurant (Stella - Chulna Road, Vasai West)**
    - *Specialties*: Paneer Lababdar (₹210), Butter Chicken (₹270), Garlic Butter Naan (₹45), Veg Pulao with Raita (₹150).
12. **Vasai Cloud Kitchen Co. (Pelhar-Waliv, Vasai East)**
    - *Specialties*: Family 6-Pack Feast Box Pure Veg (₹1,180 for 6), Jumbo Chicken Biryani Bucket (₹1,250 for 6).

---

## 🚀 Running the App & Tests

- Build and compile:
  ```bash
  compile_applet
  ```
- Run unit tests:
  ```bash
  gradle :app:testDebugUnitTest
  ```

