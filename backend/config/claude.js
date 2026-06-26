const Anthropic = require('@anthropic-ai/sdk');

const client = new Anthropic({ apiKey: process.env.ANTHROPIC_API_KEY });

/**
 * Analyzes food product ingredients using Claude AI.
 * Returns a complete, plain-English health report any consumer can understand.
 *
 * @param {string} productName
 * @param {string} brand
 * @param {string} ingredientsText
 * @param {object} nutritionJson   - per-100g values { calories, protein, carbs, fat, sugar, fiber, sodium }
 * @param {string} userPreferences - comma-separated e.g. "diabetic, vegan"
 * @returns {Promise<object>} Parsed AI analysis JSON
 */
const analyzeIngredients = async (productName, brand, ingredientsText, nutritionJson, userPreferences) => {

  const systemPrompt = `You are a friendly food safety expert inside a mobile app. When given a food product's name, brand, ingredients list, and nutritional information, analyze every single ingredient individually and return a complete health report in simple everyday language that any person without any food science knowledge can understand.

Give the product an overall health score from 1 to 10 where 1 is extremely dangerous and 10 is perfectly healthy, explain in one bold sentence why it got that score, then list every harmful ingredient with a plain explanation of exactly what it does to the human body, which organ it affects, whether it is banned in any countries, and who specifically should avoid it such as children, pregnant women, or diabetics.

Then decode every E-number or additive into plain English with its risk level as Safe, Caution, or Avoid. Compare the nutrition values against WHO daily recommended limits and clearly tell the user if sugar, sodium, or fat levels are dangerously high with a simple statement like "one can of this exceeds your entire day's sugar limit". Show personalized warnings based on the user's health profile such as diabetic, vegan, pregnant, or gluten intolerant. List any positive or beneficial ingredients with their health benefits.

Finally give a 2 to 3 sentence human-friendly verdict written like a knowledgeable friend giving honest advice and suggest what type of healthier alternative the user should look for.

Never use scientific jargon without immediately explaining it in simple words, never skip any ingredient, and always prioritize clarity and honesty.

Return ONLY a clean valid JSON object. No markdown. No explanation outside the JSON.`;

  const userPrompt = `Analyze this food product completely and return a full health report.

Product Name: ${productName || 'Unknown'}
Brand: ${brand || 'Unknown'}
Ingredients List: ${ingredientsText || 'Not available'}
Nutritional Info per 100g: ${JSON.stringify(nutritionJson || {})}
User Health Profile: ${userPreferences || 'None specified'}

Return ONLY this exact JSON structure, fully filled. Every array must have real entries (no empty arrays unless genuinely nothing applies):

{
  "health_score": <number 1-10>,
  "score_label": "<Dangerous | Poor | Moderate | Good | Excellent>",
  "score_reason": "<ONE bold sentence explaining WHY this score — be specific about the worst offenders>",

  "harmful_ingredients": [
    {
      "name": "<ingredient name in plain English>",
      "what_it_is": "<one sentence: what this ingredient actually is>",
      "body_impact": "<plain English: what it does to your body and which organ it affects>",
      "risk_level": "<High | Medium | Low>",
      "banned_in": ["<country or region if applicable>"],
      "avoid_if": "<who specifically should avoid this: children, pregnant women, diabetics, etc.>"
    }
  ],

  "safe_ingredients": [
    "<name of ingredient that is safe and harmless>"
  ],

  "additives": [
    {
      "code": "<E-number or additive name>",
      "name": "<full plain English name>",
      "purpose": "<what it does in the food e.g. preservative, colour, sweetener>",
      "risk": "<Safe | Caution | Avoid>",
      "explanation": "<one plain sentence: why this risk level, what it does to your body>"
    }
  ],

  "nutrition_flags": [
    {
      "nutrient": "<Sugar | Sodium | Fat | Saturated Fat | Calories>",
      "level": "<OK | High | Very High | Dangerously High>",
      "who_limit": "<WHO daily recommended limit for this nutrient>",
      "amount_per_100g": "<actual value from the product>",
      "plain_warning": "<simple human sentence like: one serving gives you X% of your entire daily limit>"
    }
  ],

  "allergens": [
    "<allergen name e.g. Gluten, Dairy, Nuts, Soy>"
  ],

  "personalized_warnings": [
    {
      "applies_to": "<diabetic | vegan | pregnant | gluten-free | child | keto>",
      "warning": "<plain English warning specific to this group>",
      "reason": "<which specific ingredients cause this concern>"
    }
  ],

  "positive_ingredients": [
    {
      "name": "<ingredient name>",
      "benefit": "<plain English health benefit>"
    }
  ],

  "final_verdict": "<2-3 sentences written like a knowledgeable friend giving honest advice — be direct and human, not corporate>",

  "better_alternative": "<one sentence suggesting what TYPE of healthier product to look for instead>"
}

RULES: Analyze every single ingredient — do not skip any. Use simple everyday words. If an ingredient is safe, say so clearly. If it is harmful, explain exactly why without being alarmist. Be honest and direct.`;

  const message = await client.messages.create({
    model: process.env.CLAUDE_MODEL || 'claude-3-5-sonnet-20241022',
    max_tokens: 4096,
    system: systemPrompt,
    messages: [{ role: 'user', content: userPrompt }]
  });

  const rawText = message.content[0].text.trim();

  // Strip markdown code fences if Claude wraps the JSON
  const jsonMatch = rawText.match(/\{[\s\S]*\}/);
  if (!jsonMatch) throw new Error('Claude returned a non-JSON response');

  const parsed = JSON.parse(jsonMatch[0]);

  // Normalize: ensure health_score is always a plain number at top level
  if (typeof parsed.health_score !== 'number') {
    parsed.health_score = parseInt(parsed.health_score) || 0;
  }

  return parsed;
};

module.exports = { analyzeIngredients };
