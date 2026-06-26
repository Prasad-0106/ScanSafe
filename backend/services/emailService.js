const nodemailer = require('nodemailer');

// In-memory OTP store: { email: { otp, expiresAt } }
const otpStore = new Map();

const createTransporter = () => {
  return nodemailer.createTransport({
    service: process.env.EMAIL_SERVICE || 'gmail',
    auth: {
      user: process.env.EMAIL_USER,
      pass: process.env.EMAIL_PASS
    }
  });
};

const generateOTP = () => {
  return Math.floor(100000 + Math.random() * 900000).toString();
};

const sendOTP = async (email, otp) => {
  const transporter = createTransporter();

  // Split OTP into individual digits for better display
  const digits = otp.split('');

  const mailOptions = {
    from: `"ScanSafe" <${process.env.EMAIL_USER}>`,
    to: email,
    subject: 'Your ScanSafe Password Reset OTP',
    html: `
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>ScanSafe – Password Reset</title>
</head>
<body style="margin:0;padding:0;background-color:#f0f4f0;font-family:'Segoe UI',Arial,sans-serif;">

  <table width="100%" cellpadding="0" cellspacing="0" style="background:#f0f4f0;padding:40px 16px;">
    <tr>
      <td align="center">
        <table width="100%" cellpadding="0" cellspacing="0" style="max-width:520px;">

          <!-- ── Logo Header ── -->
          <tr>
            <td align="center" style="padding-bottom:24px;">
              <table cellpadding="0" cellspacing="0">
                <tr>
                  <td style="background:#00C853;border-radius:16px;padding:12px 24px;">
                    <span style="font-size:22px;font-weight:900;color:#ffffff;letter-spacing:-0.5px;">🛡️ ScanSafe</span>
                  </td>
                </tr>
              </table>
              <p style="margin:10px 0 0;font-size:13px;color:#777;">Food Safety Scanner</p>
            </td>
          </tr>

          <!-- ── Main Card ── -->
          <tr>
            <td style="background:#ffffff;border-radius:24px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">

              <!-- Top accent strip -->
              <div style="height:6px;background:linear-gradient(90deg,#00C853,#009624);"></div>

              <table width="100%" cellpadding="0" cellspacing="0" style="padding:36px 36px 28px;">

                <!-- Title -->
                <tr>
                  <td align="center" style="padding-bottom:8px;">
                    <div style="width:56px;height:56px;background:#E8F5E9;border-radius:50%;display:inline-flex;align-items:center;justify-content:center;font-size:28px;line-height:56px;text-align:center;">🔐</div>
                  </td>
                </tr>
                <tr>
                  <td align="center" style="padding-bottom:6px;">
                    <h1 style="margin:0;font-size:24px;font-weight:800;color:#1a1a1a;letter-spacing:-0.3px;">Password Reset</h1>
                  </td>
                </tr>
                <tr>
                  <td align="center" style="padding-bottom:28px;">
                    <p style="margin:0;font-size:15px;color:#666;line-height:1.6;">
                      We received a request to reset your password.<br/>
                      Use the one-time code below to continue.
                    </p>
                  </td>
                </tr>

                <!-- OTP Box -->
                <tr>
                  <td align="center" style="padding-bottom:28px;">
                    <div style="background:#f6fef8;border:2px solid #00C853;border-radius:20px;padding:28px 20px;">
                      <p style="margin:0 0 14px;font-size:12px;font-weight:600;color:#00C853;letter-spacing:2px;text-transform:uppercase;">Your One-Time Password</p>
                      <!-- Each digit in its own cell so it NEVER wraps -->
                      <table cellpadding="0" cellspacing="0" align="center">
                        <tr>
                          ${digits.map(d => `
                          <td style="padding:0 5px;">
                            <div style="width:44px;height:56px;background:#ffffff;border:2px solid #e0e0e0;border-radius:12px;font-size:30px;font-weight:900;color:#00C853;text-align:center;line-height:56px;box-shadow:0 2px 8px rgba(0,200,83,0.1);">${d}</div>
                          </td>`).join('')}
                        </tr>
                      </table>
                      <p style="margin:18px 0 0;font-size:12px;color:#999;">Expires in <strong style="color:#1a1a1a;">10 minutes</strong></p>
                    </div>
                  </td>
                </tr>

                <!-- Info row -->
                <tr>
                  <td>
                    <table width="100%" cellpadding="0" cellspacing="0">
                      <tr>
                        <td style="background:#fff8f0;border-left:4px solid #FF6D00;border-radius:0 10px 10px 0;padding:12px 16px;">
                          <p style="margin:0;font-size:13px;color:#555;line-height:1.6;">
                            ⚠️ <strong>Never share this code</strong> with anyone. ScanSafe will never ask for your OTP via phone or chat.
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>

                <tr><td style="height:20px;"></td></tr>

                <!-- Not you? -->
                <tr>
                  <td style="background:#f5f5f5;border-radius:12px;padding:14px 18px;">
                    <p style="margin:0;font-size:13px;color:#888;line-height:1.6;">
                      🔒 <strong>Didn't request this?</strong> You can safely ignore this email. Your account remains secure and no changes have been made.
                    </p>
                  </td>
                </tr>

              </table>
            </td>
          </tr>

          <!-- ── Footer ── -->
          <tr>
            <td align="center" style="padding:24px 0 8px;">
              <p style="margin:0;font-size:12px;color:#aaa;">
                © ${new Date().getFullYear()} ScanSafe · Food Safety Scanner<br/>
                <span style="font-size:11px;">This is an automated message. Please do not reply.</span>
              </p>
            </td>
          </tr>

        </table>
      </td>
    </tr>
  </table>

</body>
</html>
    `
  };

  await transporter.sendMail(mailOptions);
};

module.exports = { otpStore, generateOTP, sendOTP };
