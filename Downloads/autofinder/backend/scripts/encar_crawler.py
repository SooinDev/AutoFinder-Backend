# 이 코드는 교육 및 개인 학습 목적이며, 웹사이트 정책을 준수해야 합니다.
import re
import pymysql
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time

# MySQL 연결 설정
conn = pymysql.connect(
    host="localhost",
    user="encar_user",  # MySQL Workbench에서 만든 사용자 계정
    password="Mysql1234!",  # 해당 계정의 비밀번호
    database="encar_db",
    charset="utf8mb4"
)
cursor = conn.cursor()

# Chromedriver 실행 파일 경로 설정
chromedriver_path = "/opt/homebrew/bin/chromedriver"

# Selenium 옵션 설정 (headless 모드로 실행)
chrome_options = Options()
chrome_options.add_argument("--headless")  # 브라우저 창을 띄우지 않고 백그라운드에서 실행
chrome_options.add_argument("--no-sandbox")
chrome_options.add_argument("--disable-dev-shm-usage")
chrome_options.add_argument("user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.123 Safari/537.36")

# Selenium WebDriver 실행
service = Service(chromedriver_path)
driver = webdriver.Chrome(service=service, options=chrome_options)

# 크롤링할 엔카 검색 URL (국산차 & 수입차)
base_urls = {
    "국산차": "http://www.encar.com/dc/dc_carsearchlist.do?carType=kor",
    "수입차": "http://www.encar.com/fc/fc_carsearchlist.do?carType=for"
}

# 국산차 & 수입차 각각 크롤링 진행
for car_type, base_url in base_urls.items():
    for page in range(1, 3):  # 2페이지까지만 크롤링 (추후 확장 가능)
        search_url = f"{base_url}&page={page}"  # 페이지 번호를 URL에 추가
        driver.get(search_url)
        time.sleep(3)  # 페이지 로딩 대기

        # 일반 차량 목록 (`tbody#sr_normal` 내부 데이터만 가져오기)
        try:
            car_table = WebDriverWait(driver, 10).until(
                EC.presence_of_element_located((By.ID, "sr_normal"))  # 광고 차량이 아닌 일반 차량 목록
            )
            car_rows = car_table.find_elements(By.TAG_NAME, "tr")  # 일반 차량 리스트 가져오기
            print(f"[{car_type}] {page}페이지 일반 차량 목록 로드 완료! 차량 개수:", len(car_rows))
        except:
            print(f"[{car_type}] {page}페이지 일반 차량 목록을 찾을 수 없습니다.")
            continue

        # 개별 차량 정보 크롤링 시작
        for car in car_rows:
            try:
                # 차량명 및 주요 정보 추출 (`td.inf` 요소)
                try:
                    name_element = car.find_element(By.CSS_SELECTOR, "td.inf")
                    raw_text = name_element.text.strip()

                    # 줄바꿈 제거하여 한 줄로 변환
                    raw_text = " ".join(raw_text.split("\n"))

                    # 차종 추출 (첫 번째 단어 기준)
                    car_model_match = re.search(r"^[가-힣A-Za-z0-9\s\-\(\)\.]+", raw_text)
                    car_model = car_model_match.group(0).strip() if car_model_match else "차종 정보 없음"

                    # 차량명이 2자리 숫자로 끝나는 경우(연식 제거)
                    if re.search(r"\s\d{2}$", car_model):  # 공백 + 숫자 2자리로 끝나는 경우
                        car_model = car_model.rsplit(" ", 1)[0]  # 마지막 공백 이후 문자열 제거

                    # 연식 추출 (예: "22/01식" 또는 "18/03식(19년형)")
                    year_match = re.search(r"(\d{2}/\d{2}식|\d{2}/\d{2}식\(\d{2}년형\))", raw_text)
                    year = year_match.group(0) if year_match else "연식 정보 없음"

                    # 주행거리(키로수) 추출 (예: "67,427km" → 67427)
                    km_match = re.search(r"(\d{1,3}(?:,\d{3})*)km", raw_text)
                    if km_match:
                        km = int(km_match.group(1).replace(",", ""))  # 쉼표 제거 후 정수 변환
                    else:
                        km = None  # 데이터 없을 경우 NULL 저장

                    # 차량 가격 추출 (예: "15,000,000원")
                    try:
                        price_element = car.find_element(By.CSS_SELECTOR, "td.prc_hs strong")
                        price_text = price_element.text.strip().replace(",", "").replace("원", "")  # 쉼표와 '원' 제거
                        price = int(price_text) if price_text.isdigit() else 0  # 숫자로 변환
                        print(f"[디버깅] 추출된 가격: {price_text} → 변환된 가격: {price}")
                    except:
                        print("[디버깅] 가격 정보를 찾을 수 없음")
                        price = 0  # 가격이 없을 경우 기본값 0

                    # 연료 타입 추출 (가솔린, 디젤, 하이브리드 등)
                    fuel_match = re.search(r"(가솔린|디젤|LPG|하이브리드|전기)", raw_text)
                    fuel = fuel_match.group(0) if fuel_match else "연료 정보 없음"

                    # 지역 정보 추출 (예: "서울", "경기", "부산" 등)
                    region_match = re.search(r"(서울|경기|부산|대구|대전|광주|울산|인천|강원|충청|전라|경상|제주)", raw_text)
                    region = region_match.group(0) if region_match else "지역 정보 없음"

                except:
                    print("차량명 없음, 제외 처리")
                    continue  # 차량명 없는 경우 제외

                # 차량 상세 페이지 링크 추출
                try:
                    link_element = car.find_element(By.TAG_NAME, "a")
                    link = link_element.get_attribute("href")
                except:
                    link = "URL 없음"

                # 차량 이미지 URL 추출 (수정된 코드 적용)
                try:
                    image_element = car.find_element(By.CSS_SELECTOR, "td.img img")  # 차량 이미지 태그 선택
                    image_url = image_element.get_attribute("data-src") or image_element.get_attribute("src")  # 올바른 URL 속성 가져오기
                except:
                    image_url = "이미지 없음"  # 이미지가 없을 경우 기본값

                # MySQL에 저장할 데이터 정리 (image_url 추가)
                car_data = (car_type, car_model, year, km, price, fuel, region, link, image_url)

                # 데이터 삽입 쿼리 실행 (중복 방지)
                query = """
                INSERT INTO cars (car_type, model, year, mileage, price, fuel, region, url, image_url)
                SELECT %s, %s, %s, %s, %s, %s, %s, %s, %s FROM DUAL
                WHERE NOT EXISTS (
                    SELECT 1 FROM cars 
                    WHERE model = %s AND year = %s AND mileage = %s AND price = %s AND fuel = %s
                )
                """
                cursor.execute(query, car_data + (car_model, year, km, price, fuel))
                conn.commit()

                print(f"[{car_type}] 저장 완료: {car_model}, {year}, {km}, {fuel}, {region}, {link}, {image_url}")

            except Exception as e:
                print("개별 차량 크롤링 오류 발생:", e)

# WebDriver 종료
driver.quit()

# MySQL 연결 종료
cursor.close()
conn.close()
print("크롤링 완료 및 데이터베이스 저장 완료!")
